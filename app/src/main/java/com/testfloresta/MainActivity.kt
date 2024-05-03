package com.testfloresta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.testfloresta.databinding.ActivityMainBinding
import uniffi.floresta.Config
import uniffi.floresta.Florestad
import uniffi.floresta.Network
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var daemon: Florestad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        val datadir = application.dataDir.toString()
        val config = Config(
            false,
            listOf(),
            true,
            Network.SIGNET,
            datadir,
            electrumAddress = "127.0.0.1:50001",
            logToFile = true
        )
        daemon = Florestad.fromConfig(config)
        daemon.start()

        val t = Thread(fun() {
            while (true) {
                val client = Socket("127.0.0.1", 50001)
                client.outputStream.write(
                    "{\"jsonrpc\":\"2.0\",\"method\":\"blockchain.headers.subscribe\",\"params\":[],\"id\":1}\n"
                        .toByteArray()
                )
                val text = BufferedReader(InputStreamReader(client.inputStream)).readLine()
                runOnUiThread {
                    findViewById<TextView>(R.id.textView).text = text
                }
                Thread.sleep(10000)
            }
        })

        t.start()
    }

    override fun onDestroy() {
        daemon.stop()
        super.onDestroy()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}