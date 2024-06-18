/*
 *  Copyright (c) 2022~2024 chr_56
 */

package player.phonograph.ui.modules.playlist.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import player.phonograph.R
import player.phonograph.mechanism.playlist.PlaylistProcessors
import player.phonograph.model.Song
import player.phonograph.repo.mediastore.loaders.PlaylistLoader
import player.phonograph.util.parcelableArrayList
import player.phonograph.util.theme.tintButtons
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatePlaylistDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs = requireArguments().parcelableArrayList<Song>(SONGS)!!
        return MaterialDialog(requireActivity())
            .title(R.string.new_playlist_title)
            .positiveButton(R.string.create_action)
            .negativeButton(android.R.string.cancel)
            .input(
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                hintRes = R.string.playlist_name_empty,
                waitForPositiveButton = true,
                allowEmpty = false
            ) { _, input ->
                val name = input.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.failed), Toast.LENGTH_SHORT).show()
                    return@input
                }
                val activity = requireActivity()
                if (!PlaylistLoader.checkExistence(activity, name)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        PlaylistProcessors.create(
                            context = activity,
                            name = name,
                            songs = songs,
                        )
                    }

                } else {
                    Toast.makeText(
                        activity,
                        requireActivity().resources.getString(
                            R.string.playlist_exists,
                            name
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .tintButtons()
    }

    companion object {
        private const val SONGS = "songs"

        fun create(songs: List<Song>?): CreatePlaylistDialog =
            CreatePlaylistDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(SONGS, ArrayList(songs ?: emptyList()))
                }
            }

    }
}
