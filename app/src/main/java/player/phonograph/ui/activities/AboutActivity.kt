package player.phonograph.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import chr_56.MDthemer.core.ThemeColor
import de.psdev.licensesdialog.LicensesDialog
import player.phonograph.App.Companion.instance
import player.phonograph.R
import player.phonograph.databinding.ActivityAboutBinding
import player.phonograph.dialogs.ChangelogDialog.Companion.create
import player.phonograph.ui.activities.base.ThemeActivity
import player.phonograph.ui.activities.bugreport.BugReportActivity
import player.phonograph.ui.activities.intro.AppIntroActivity

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class AboutActivity : ThemeActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAboutBinding

    private lateinit var mToolbar: Toolbar
    private lateinit var appVersion: TextView
    private lateinit var changelog: LinearLayout
    private lateinit var intro: LinearLayout
    private lateinit var licenses: LinearLayout
    private lateinit var writeAnEmail: LinearLayout
    private lateinit var followOnTwitter: LinearLayout
    private lateinit var forkOnGitHub: LinearLayout
    private lateinit var visitWebsite: LinearLayout
    private lateinit var reportBugs: LinearLayout
    private lateinit var translate: LinearLayout
    private lateinit var rateOnGooglePlay: LinearLayout
    private lateinit var cracked: LinearLayout
    private lateinit var aidanFollestadGitHub: AppCompatButton
    private lateinit var michaelCookWebsite: AppCompatButton
    private lateinit var maartenCorpelWebsite: AppCompatButton
    private lateinit var maartenCorpelTwitter: AppCompatButton
    private lateinit var aleksandarTesicTwitter: AppCompatButton
    private lateinit var eugeneCheungGitHub: AppCompatButton
    private lateinit var eugeneCheungWebsite: AppCompatButton
    private lateinit var adrianTwitter: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        binding()
        mToolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setDrawUnderStatusbar()
        //        ButterKnife.bind(this);
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setUpViews()
    }

    private fun binding() {
        appVersion = binding.activityAboutMainContent.cardAboutAppLayout.appVersion
        changelog = binding.activityAboutMainContent.cardAboutAppLayout.changelog
        licenses = binding.activityAboutMainContent.cardAboutAppLayout.licenses
        forkOnGitHub = binding.activityAboutMainContent.cardAboutAppLayout.forkOnGithub
        writeAnEmail = binding.activityAboutMainContent.cardAuthorLayout.writeAnEmail
        followOnTwitter = binding.activityAboutMainContent.cardAuthorLayout.followOnTwitter
        visitWebsite = binding.activityAboutMainContent.cardAuthorLayout.visitWebsite
        intro = binding.activityAboutMainContent.cardSupportDevelopmentLayout.intro
        reportBugs = binding.activityAboutMainContent.cardSupportDevelopmentLayout.reportBugs
        translate = binding.activityAboutMainContent.cardSupportDevelopmentLayout.translate
        rateOnGooglePlay =
            binding.activityAboutMainContent.cardSupportDevelopmentLayout.rateOnGooglePlay
        cracked = binding.activityAboutMainContent.cardSupportDevelopmentLayout.cracked
        aidanFollestadGitHub =
            binding.activityAboutMainContent.cardSpecialThanksLayout.aidanFollestadGitHub
        michaelCookWebsite =
            binding.activityAboutMainContent.cardSpecialThanksLayout.michaelCookWebsite
        maartenCorpelTwitter =
            binding.activityAboutMainContent.cardSpecialThanksLayout.maartenCorpelTwitter
        maartenCorpelWebsite =
            binding.activityAboutMainContent.cardSpecialThanksLayout.maartenCorpelWebsite
        aleksandarTesicTwitter =
            binding.activityAboutMainContent.cardSpecialThanksLayout.aleksandarTesicTwitter
        eugeneCheungGitHub =
            binding.activityAboutMainContent.cardSpecialThanksLayout.eugeneCheungGitHub
        eugeneCheungWebsite =
            binding.activityAboutMainContent.cardSpecialThanksLayout.eugeneCheungWebsite
        adrianTwitter = binding.activityAboutMainContent.cardSpecialThanksLayout.adrianTwitter
    }

    private fun setUpViews() {
        setUpToolbar()
        setUpAppVersion()
        setUpOnClickListeners()
    }

    private fun setUpToolbar() {
        mToolbar.setBackgroundColor(ThemeColor.primaryColor(this))
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpAppVersion() {
        appVersion.text = getCurrentVersionName(this)
    }

    private fun setUpOnClickListeners() {
        changelog.setOnClickListener(this)
        intro.setOnClickListener(this)
        licenses.setOnClickListener(this)
        followOnTwitter.setOnClickListener(this)
        forkOnGitHub.setOnClickListener(this)
        visitWebsite.setOnClickListener(this)
        reportBugs.setOnClickListener(this)
        writeAnEmail.setOnClickListener(this)
        translate.setOnClickListener(this)
        rateOnGooglePlay.setOnClickListener(this)
        cracked.setOnClickListener(this)
        aidanFollestadGitHub.setOnClickListener(this)
        michaelCookWebsite.setOnClickListener(this)
        maartenCorpelWebsite.setOnClickListener(this)
        maartenCorpelTwitter.setOnClickListener(this)
        aleksandarTesicTwitter.setOnClickListener(this)
        eugeneCheungGitHub.setOnClickListener(this)
        eugeneCheungWebsite.setOnClickListener(this)
        adrianTwitter.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v) {
            changelog -> {
                create().show(supportFragmentManager, "CHANGELOG_DIALOG")
            }
            licenses -> {
                showLicenseDialog()
            }
            intro -> {
                startActivity(Intent(this, AppIntroActivity::class.java))
            }
            followOnTwitter -> {
                openUrl(TWITTER)
            }
            forkOnGitHub -> {
                openUrl(GITHUB)
            }
            visitWebsite -> {
                openUrl(WEBSITE)
            }
            reportBugs -> {
                startActivity(Intent(this, BugReportActivity::class.java))
            }
            writeAnEmail -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:contact@kabouzeid.com")
                intent.putExtra(Intent.EXTRA_EMAIL, "contact@kabouzeid.com")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Phonograph")
                startActivity(Intent.createChooser(intent, "E-Mail"))
            }
            translate -> {
                openUrl(TRANSLATE)
            }
            rateOnGooglePlay -> {
                openUrl(RATE_ON_GOOGLE_PLAY)
            }
            cracked -> {
                openUrl("https://github.com/chr56/Phonograph_Plus")
                Toast.makeText(this, R.string.description_cracked, Toast.LENGTH_SHORT).show()
            }
            aidanFollestadGitHub -> {
                openUrl(AIDAN_FOLLESTAD_GITHUB)
            }
            michaelCookWebsite -> {
                openUrl(MICHAEL_COOK_WEBSITE)
            }
            maartenCorpelWebsite -> {
                openUrl(MAARTEN_CORPEL_WEBSITE)
            }
            maartenCorpelTwitter -> {
                openUrl(MAARTEN_CORPEL_TWITTER)
            }
            aleksandarTesicTwitter -> {
                openUrl(ALEKSANDAR_TESIC_TWITTER)
            }
            eugeneCheungGitHub -> {
                openUrl(EUGENE_CHEUNG_GITHUB)
            }
            eugeneCheungWebsite -> {
                openUrl(EUGENE_CHEUNG_WEBSITE)
            }
            adrianTwitter -> {
                openUrl(ADRIAN_TWITTER)
            }
        }
        //        Test Only
//        throw new RuntimeException("Crash Test"); // Crash Test
    }

    private fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun showLicenseDialog() {
        val app = instance
        LicensesDialog.Builder(this)
            .setNotices(R.raw.notices)
            .setTitle(R.string.licenses)
            .setNoticesCssStyle(
                getString(R.string.license_dialog_style)
                    .replace("{bg-color}", if (app.nightmode()) "424242" else "ffffff")
                    .replace("{text-color}", if (app.nightmode()) "ffffff" else "000000")
                    .replace("{license-bg-color}", if (app.nightmode()) "535353" else "eeeeee")
            )
            .setIncludeOwnLicense(true)
            .build()
            .show()
        //        Test Only
//        throw new RuntimeException("Crash Test"); // crash test
    }

    companion object {
        private const val GITHUB = "https://github.com/kabouzeid/Phonograph"
        private const val TWITTER = "https://twitter.com/swiftkarim"
        private const val WEBSITE = "https://kabouzeid.com/"
        private const val TRANSLATE =
            "https://phonograph.oneskyapp.com/collaboration/project?id=26521"
        private const val RATE_ON_GOOGLE_PLAY =
            "https://play.google.com/store/apps/details?id=com.kabouzeid.gramophone"
        private const val AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad"
        private const val MICHAEL_COOK_WEBSITE = "https://cookicons.co/"
        private const val MAARTEN_CORPEL_WEBSITE = "https://maartencorpel.com/"
        private const val MAARTEN_CORPEL_TWITTER = "https://twitter.com/maartencorpel"
        private const val ALEKSANDAR_TESIC_TWITTER = "https://twitter.com/djsalezmaj"
        private const val EUGENE_CHEUNG_GITHUB = "https://github.com/arkon"
        private const val EUGENE_CHEUNG_WEBSITE = "https://echeung.me/"
        private const val ADRIAN_TWITTER = "https://twitter.com/froschgames"
        private fun getCurrentVersionName(context: Context): String {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "Unkown"
        }
    }
}
