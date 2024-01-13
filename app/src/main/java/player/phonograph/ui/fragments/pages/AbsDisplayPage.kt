/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.ui.fragments.pages

import com.github.chr56.android.menu_dsl.attach
import com.github.chr56.android.menu_dsl.menuItem
import com.google.android.material.appbar.AppBarLayout
import mt.pref.ThemeColor
import mt.util.color.primaryTextColor
import player.phonograph.App
import player.phonograph.R
import player.phonograph.databinding.FragmentDisplayPageBinding
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.model.Displayable
import player.phonograph.model.sort.SortMode
import player.phonograph.ui.adapter.ConstDisplayConfig
import player.phonograph.ui.adapter.DisplayAdapter
import player.phonograph.ui.adapter.ItemLayoutStyle
import player.phonograph.ui.components.popup.ListOptionsPopup
import player.phonograph.util.debug
import player.phonograph.util.theme.getTintedDrawable
import player.phonograph.util.theme.nightMode
import player.phonograph.util.ui.setUpFastScrollRecyclerViewColor
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.recyclerview.widget.GridLayoutManager
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu.NONE
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.launch


/**
 * Page Fragment for displaying [IT] items.
 * @param IT the model type that this fragment displays
 * @param A used Adapter
 */
sealed class AbsDisplayPage<IT : Displayable, A : DisplayAdapter<IT>> : AbsPage() {

    private var _viewBinding: FragmentDisplayPageBinding? = null
    private val binding get() = _viewBinding!!

    protected abstract val viewModel: AbsDisplayPageViewModel<IT>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewModel.loadDataset(requireContext())
        _viewBinding = FragmentDisplayPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // for mini player bar
    private var outerAppbarOffsetListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            binding.container.setPadding(
                binding.container.paddingLeft,
                binding.container.paddingTop,
                binding.container.paddingRight,
                hostFragment.totalAppBarScrollingRange + verticalOffset
            )
        }

    private var innerAppbarOffsetListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            binding.container.setPadding(
                binding.container.paddingLeft,
                binding.panel.totalScrollRange + verticalOffset,
                binding.container.paddingRight,
                binding.container.paddingBottom

            )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.empty.text = resources.getText(R.string.loading)

        initRecyclerView()
        initAppBar()

        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.dataSet.collect {
                    checkEmpty()
                    adapter.dataset = it.toList()
                    updateHeaderText()
                }
            }
        }
    }


    protected open val emptyMessage: Int @StringRes get() = R.string.empty
    private fun checkEmpty() {
        binding.empty.setText(emptyMessage)
        binding.empty.visibility = if (viewModel.isEmpty) View.VISIBLE else View.GONE
    }

    private fun updateHeaderText() {
        binding.panelText.text = viewModel.headerText(requireContext())
    }


    protected lateinit var adapter: A
    protected lateinit var layoutManager: GridLayoutManager

    protected abstract fun initAdapter(): A

    protected abstract fun displayConfig(): PageDisplayConfig
    protected var adapterDisplayConfig: ConstDisplayConfig = ConstDisplayConfig(ItemLayoutStyle.LIST)

    private fun initRecyclerView() {

        layoutManager = GridLayoutManager(hostFragment.requireContext(), displayConfig().gridSize)
        adapter = initAdapter()

        binding.recyclerView.setUpFastScrollRecyclerViewColor(
            hostFragment.mainActivity,
            ThemeColor.accentColor(App.instance.applicationContext)
        )
        binding.recyclerView.also {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
    }

    private fun initAppBar() {

        binding.panel.setExpanded(false)
        binding.panel.addOnOffsetChangedListener(innerAppbarOffsetListener)

        val context = hostFragment.mainActivity
        context.attach(binding.panelToolbar.menu) {
            menuItem(NONE, NONE, 999, getString(R.string.action_settings)) {
                icon = context.getTintedDrawable(
                    R.drawable.ic_sort_variant_white_24dp,
                    context.primaryTextColor(context.nightMode),
                )
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_ALWAYS
                onClick {
                    hostFragment.popup.onShow = ::configPopup
                    hostFragment.popup.onDismiss = ::dismissPopup
                    hostFragment.popup.showAtLocation(
                        binding.root, Gravity.TOP or Gravity.END, 0,
                        8 + hostFragment.totalHeaderHeight + binding.panel.height
                    )
                    true
                }
            }
        }

        binding.panelText.setTextColor(context.primaryTextColor(context.nightMode))
        binding.panelToolbar.setTitleTextColor(requireContext().primaryTextColor(requireContext().nightMode))

        configAppBar(binding.panelToolbar)

        hostFragment.addOnAppBarOffsetChangedListener(outerAppbarOffsetListener)
    }

    protected open fun configAppBar(panelToolbar: Toolbar) {}

    private fun configPopup(popup: ListOptionsPopup) {
        popup.setup(displayConfig())
    }

    @SuppressLint("NotifyDataSetChanged")
    protected fun dismissPopup(popup: ListOptionsPopup) {
        val displayConfig = displayConfig()
        var update = false

        // layout
        val layoutSelected = popup.itemLayout
        if (displayConfig.updateItemLayout(layoutSelected)) {
            update = true
            adapterDisplayConfig = adapterDisplayConfig.copy(layoutStyle = layoutSelected)
        }

        // grid size
        val gridSizeSelected = popup.gridSize
        if (gridSizeSelected > 0 && gridSizeSelected != displayConfig.gridSize) {
            displayConfig.gridSize = gridSizeSelected
            layoutManager.spanCount = gridSizeSelected
        }

        // color footer
        if (displayConfig.allowColoredFooter) {
            val coloredFootersSelected = popup.colorFooter
            if (displayConfig.colorFooter != coloredFootersSelected) {
                displayConfig.colorFooter = coloredFootersSelected
                update = true
                adapterDisplayConfig = adapterDisplayConfig.copy(useImageText = coloredFootersSelected)
            }
        }

        // sort order
        val selected = SortMode(popup.sortRef, popup.revert)
        if (displayConfig.updateSortMode(selected)) {
            viewModel.loadDataset(requireContext())
        }

        if (update) {
            adapter.config = adapterDisplayConfig
            adapter.notifyDataSetChanged()
        }
        checkValidation(displayConfig)
    }

    private fun checkValidation(displayConfig: PageDisplayConfig) {
        var warningLayout: Boolean =
            when (displayConfig.layout) {
                ItemLayoutStyle.GRID    -> displayConfig.gridSize <= 2
                ItemLayoutStyle.LIST_3L -> displayConfig.gridSize > 3
                else                    -> displayConfig.gridSize > 2
            }
        if (warningLayout) {
            Toast.makeText(requireContext(), R.string.warning_inappropriate_config, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.panel.removeOnOffsetChangedListener(innerAppbarOffsetListener)
        hostFragment.removeOnAppBarOffsetChangedListener(outerAppbarOffsetListener)
        _viewBinding = null
    }

    private lateinit var listener: MediaStoreListener
    override fun onCreate(savedInstanceState: Bundle?) {
        listener = MediaStoreListener()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(listener)
    }


    private inner class MediaStoreListener : MediaStoreTracker.LifecycleListener() {
        override fun onMediaStoreChanged() {
            lifecycleScope.launch {
                lifecycle.withResumed {
                    viewModel.loadDataset(requireContext())
                }
            }
        }
    }

    protected val addNewItemButton get() = binding.addNewItem

    override fun onResume() {
        super.onResume()
        debug { Log.v("Metrics", "${System.currentTimeMillis().mod(10000000)} AbsDisplayPage.onResume()") }
    }
}
