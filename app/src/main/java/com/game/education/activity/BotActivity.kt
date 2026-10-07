package com.game.education.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import com.game.education.bot.JomlakBot
import com.game.education.bot.LogAdapter
import com.game.education.databinding.ActivityBotBinding
import com.game.education.model.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBotBinding
    private lateinit var logAdapter: LogAdapter
    private lateinit var prefs: Prefs
    private var bot: JomlakBot? = null
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ prefs قبل از checkLoginStatus مقداردهی شود
        prefs = Prefs(this)

        setupRecyclerView()
        setupButtons()
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val username = prefs.GetUsername()
        if (username.isNullOrEmpty()) {
            appendLog("لطفا ابتدا در برنامه اصلی وارد شوید")
            Toast.makeText(this, "لطفا ابتدا وارد شوید", Toast.LENGTH_LONG).show()
            binding.btnStart.isEnabled = false
        } else {
            appendLog("کاربر وارد شده: $username")
            binding.btnStart.isEnabled = true
            binding.btnLogin.visibility = View.GONE
            binding.etUsername.visibility = View.GONE
            binding.etPassword.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        logAdapter = LogAdapter()
        binding.recyclerLogs.apply {
            layoutManager = LinearLayoutManager(this@BotActivity)
            adapter = logAdapter
        }
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener { login() }
        binding.btnStart.setOnClickListener { startBot() }
        binding.btnSendEmail.setOnClickListener { sendEmail() }
        binding.btnClear.setOnClickListener {
            logAdapter.clear()
            appendLog("لاگ پاک شد")
        }
    }

    private fun login() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "نام کاربری و رمز عبور را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface::class.java)
        val call = apiInterface.login(username, password)

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "در حال ورود..."

        call.enqueue(object : Callback<ServerData.Message> {
            override fun onResponse(call: Call<ServerData.Message>, response: Response<ServerData.Message>) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "ورود"

                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()!!.result
                    prefs.SetUsername(username)
                    prefs.SetToken(result.token)
                    result.affiliateCode?.let { prefs.SaveCode(it) }

                    appendLog("ورود موفق!")
                    binding.btnStart.isEnabled = true
                    checkLoginStatus()
                } else {
                    appendLog("ورود ناموفق")
                    Toast.makeText(this@BotActivity, "ورود ناموفق", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerData.Message>, t: Throwable) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "ورود"
                appendLog("خطا: ${t.message}")
            }
        })
    }

    private fun startBot() {
        if (isRunning) {
            Toast.makeText(this, "ربات در حال اجراست", Toast.LENGTH_SHORT).show()
            return
        }

        val token = prefs.GetToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "لطفا ابتدا وارد شوید", Toast.LENGTH_SHORT).show()
            return
        }

        isRunning = true
        binding.btnStart.isEnabled = false
        binding.btnStart.text = "در حال اجرا..."

        // ✅ توکن با Bearer اضافه شد (سرور انتظار Bearer دارد)
        bot = JomlakBot(
            context = applicationContext,
            token = "Bearer $token",
            onLog = { message -> runOnUiThread { appendLog(message) } }
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    bot?.run()
                }
            } catch (e: Exception) {
                appendLog("خطا: ${e.message}")
                Log.e("BotActivity", "Bot error", e)
            } finally {
                isRunning = false
                binding.btnStart.isEnabled = true
                binding.btnStart.text = "شروع ربات"
            }
        }
    }

    private fun sendEmail() {
        val emailUser = binding.etEmailUser.text.toString().trim()
        val emailPass = binding.etEmailPass.text.toString().trim()
        val emailTo = binding.etEmailTo.text.toString().trim()

        if (emailUser.isEmpty() || emailPass.isEmpty() || emailTo.isEmpty()) {
            Toast.makeText(this, "اطلاعات ایمیل را کامل کنید", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.btnSendEmail.isEnabled = false
            binding.btnSendEmail.text = "در حال ارسال..."

            val success = withContext(Dispatchers.IO) {
                bot?.sendEmailReport(emailUser, emailPass, emailTo) ?: false
            }

            binding.btnSendEmail.isEnabled = true
            binding.btnSendEmail.text = "ارسال ایمیل"

            Toast.makeText(
                this@BotActivity,
                if (success) "ایمیل ارسال شد" else "ارسال ناموفق",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun appendLog(message: String) {
        logAdapter.addLog(message)
        val targetPos = logAdapter.itemCount - 1
        if (targetPos >= 0) {
            binding.recyclerLogs.scrollToPosition(targetPos)
        }
    }
}