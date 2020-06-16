package protect.budgetwatch

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.google.common.collect.ImmutableMap
import protect.budgetwatch.BudgetActivity
import protect.budgetwatch.ImportExportActivity
import protect.budgetwatch.intro.IntroActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val menuItems: MutableList<MainMenuItem> = LinkedList()
        menuItems.add(MainMenuItem(R.drawable.purse, R.string.budgetsTitle,
                R.string.budgetDescription))
        menuItems.add(MainMenuItem(R.drawable.transaction, R.string.transactionsTitle,
                R.string.transactionsDescription))
        val buttonList = findViewById<View>(R.id.list) as ListView
        val buttonListAdapter = MenuAdapter(this, menuItems)
        buttonList.adapter = buttonListAdapter
        buttonList.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position) as MainMenuItem
            if (item == null) {
                Log.w(TAG, "Clicked menu item at position $position is null")
                return@OnItemClickListener
            }
            var goalClass: Class<*>? = null
            when (item.menuTextId) {
                R.string.budgetsTitle -> goalClass = BudgetActivity::class.java
                R.string.transactionsTitle -> goalClass = TransactionActivity::class.java
                else -> Log.w(TAG, "Unexpected menu text id: " + item.menuTextId)
            }
            if (goalClass != null) {
                val i = Intent(applicationContext, goalClass)
                startActivity(i)
            }
        }
        val prefs = getSharedPreferences("protect.budgetwatch", Context.MODE_PRIVATE)
        if (prefs.getBoolean("firstrun", true)) {
            startIntro()
            prefs.edit().putBoolean("firstrun", false).commit()
        }
    }

    internal class MainMenuItem(val iconId: Int, val menuTextId: Int, val menuDescId: Int)

    internal class MenuAdapter(context: Context?, items: List<MainMenuItem>?) : ArrayAdapter<MainMenuItem?>(
        context!!, 0, items!!
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Get the data item for this position
            var convertView = convertView
            val item = getItem(position)

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.main_button,
                        parent, false)
            }
            val menuText = convertView!!.findViewById<View>(R.id.menu) as TextView
            val menuDescText = convertView!!.findViewById<View>(R.id.menudesc) as TextView
            val icon = convertView!!.findViewById<View>(R.id.image) as ImageView
            menuText.setText(item!!.menuTextId)
            menuDescText.setText(item.menuDescId)
            icon.setImageResource(item.iconId)
            return convertView
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_import_export) {
            val i = Intent(applicationContext, ImportExportActivity::class.java)
            startActivity(i)
            return true
        }
        if (id == R.id.action_settings) {
            val i = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(i)
            return true
        }
        if (id == R.id.action_intro) {
            startIntro()
            return true
        }
        if (id == R.id.action_about) {
            displayAboutDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayAboutDialog() {
        val USED_LIBRARIES: Map<String, String> = ImmutableMap.of(
                "Commons CSV", "https://commons.apache.org/proper/commons-csv/",
                "Guava", "https://github.com/google/guava",
                "AppIntro", "https://github.com/apl-devs/AppIntro"
        )
        val USED_ASSETS: Map<String, String> = ImmutableMap.of(
                "Piggy Bank by Icons8", "https://thenounproject.com/term/piggy-bank/61478/",
                "Purse by Dima Lagunov", "https://thenounproject.com/term/purse/26896/",
                "Ticket Bill by naim", "https://thenounproject.com/term/ticket-bill/634398/",
                "Purchase Order by Icons8", "https://icons8.com/web-app/for/all/purchase-order",
                "Save by Bernar Novalyi", "https://thenounproject.com/term/save/716011"
        )
        val libs = StringBuilder().append("<ul>")
        for ((key, value) in USED_LIBRARIES) {
            libs.append("<li><a href=\"").append(value).append("\">").append(key).append("</a></li>")
        }
        libs.append("</ul>")
        val resources = StringBuilder().append("<ul>")
        for ((key, value) in USED_ASSETS) {
            resources.append("<li><a href=\"").append(value).append("\">").append(key).append("</a></li>")
        }
        resources.append("</ul>")
        val appName = getString(R.string.app_name)
        val year = Calendar.getInstance()[Calendar.YEAR]
        var version = "?"
        try {
            val manager = packageManager
            if (manager != null) {
                val pi = manager.getPackageInfo(packageName, 0)
                version = pi.versionName
            } else {
                Log.w(TAG, "Package name not found, PackageManager unavailable")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package name not found", e)
        }
        val wv = WebView(this)
        val html = "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />" +
                "<img src=\"file:///android_res/mipmap/ic_launcher.png\" alt=\"" + appName + "\"/>" +
                "<h1>" + String.format(getString(R.string.about_title_fmt),
                "<a href=\"" + getString(R.string.app_webpage_url)) + "\">" +
                appName +
                "</a>" +
                "</h1><p>" +
                appName +
                " " + String.format(getString(R.string.debug_version_fmt), version) +
                "</p><p>" + String.format(getString(R.string.app_revision_fmt),
                "<a href=\"" + getString(R.string.app_revision_url) + "\">" +
                        getString(R.string.app_revision_url) +
                        "</a>") +
                "</p><hr/><p>" + String.format(getString(R.string.app_copyright_fmt), year) +
                "</p><hr/><p>" +
                getString(R.string.app_license) +
                "</p><hr/><p>" + String.format(getString(R.string.app_libraries), appName, libs.toString()) +
                "</p><hr/><p>" + String.format(getString(R.string.app_resources), appName, resources.toString())
        wv.loadDataWithBaseURL("file:///android_res/drawable/", html, "text/html", "utf-8", null)
        AlertDialog.Builder(this)
                .setView(wv)
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun startIntro() {
        val intent = Intent(this, IntroActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "BudgetWatch"
    }
}