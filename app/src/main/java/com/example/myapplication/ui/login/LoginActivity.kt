package com.example.myapplication.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.*
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.security.MessageDigest


const val RC_SIGN_IN = 123

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleLoginButton.setSize(SignInButton.SIZE_WIDE)
        binding.googleLoginButton.setOnClickListener{
            val signInIntent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)
        }

        val username = binding.username
        val password = binding.password
        val usernameLogin = binding.usernameLogin
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.initIntent(this)

        if(isLoggedWithUsername()){
            loginUsername(
                getLoggedWithUsernameUsername(),
                getLoggedWithUsernamePassword(),
                getLoggedWithUsernameEmail())
        }

        // if previously logged in with google, relog directly
        if(isLoggedWithGoogle()) {
            val signInIntent = mGoogleSignInClient.signInIntent
            resultLauncher.launch(signInIntent)
        }

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            usernameLogin?.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                hashPassword(password.text.toString())
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    hashPassword(password.text.toString())
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.loginCall(
                            username.text.toString(),"",
                            hashPassword(password.text.toString()), ""
                        )
                }
                false
            }

            usernameLogin?.setOnClickListener{
                loading.visibility = View.VISIBLE
                loginUsername(username.text.toString(),
                    hashPassword(password.text.toString()),
                    username.text.toString())
                loading.visibility = View.GONE
            }
        }
    }

    private fun hashPassword(plaintext: String): String{
        val byteArrayPlaintext = plaintext.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(byteArrayPlaintext)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun loginUsername(username: String,password: String, email: String){
        loginViewModel.intentPutExtra("photo","")
        loginViewModel.intentPutExtra("email", email)
        // Request for user id
        loginViewModel.loginCall(
            username,"",
            password, "")
    }

    private fun isLoggedWithGoogle(): Boolean {
        val sharedPreferences = this.getSharedPreferences("LogPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedWithGoogle", false)
    }

    private fun isLoggedWithUsername(): Boolean {
        val sharedPreferences = this.getSharedPreferences("LogPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedWithUsername", false)
    }

    private fun getLoggedWithUsernameUsername(): String {
        val sharedPreferences = this.getSharedPreferences("LogPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "").toString()
    }

    private fun getLoggedWithUsernamePassword(): String {
        val sharedPreferences = this.getSharedPreferences("LogPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("password", "").toString()
    }

    private fun getLoggedWithUsernameEmail(): String {
        val sharedPreferences = this.getSharedPreferences("LogPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("email", "").toString()
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            loginViewModel.intentPutExtra("photo",account.photoUrl.toString())
            loginViewModel.intentPutExtra("email",account.email.toString())
            loginViewModel.intentPutExtra("name",account.displayName.toString())
            // Request for user id
            loginViewModel.loginCall(account.email, account.displayName,"", account.id)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(applicationContext,"something went wrong",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}