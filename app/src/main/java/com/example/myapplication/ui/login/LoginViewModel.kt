package com.example.myapplication.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import com.example.myapplication.MainActivity
import com.example.myapplication.data.LoginRepository
import com.example.myapplication.data.Result

import com.example.myapplication.R
import org.json.JSONObject

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    private lateinit var mainIntent : Intent
    private lateinit var thisContext: Context

    fun initIntent(context: Context){
        thisContext = context
        mainIntent = Intent(context, MainActivity::class.java)
    }

    fun intentPutExtra(key: String, value: String){
        mainIntent.putExtra(key,value)
    }
/*
    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }*/

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun loginCall(email: String?, name: String?, password: String?, googleid: String?){
        val params: MutableMap<String?, String?> = HashMap()
        params["id"] = ""
        params["email"] = email
        params["password"] = password
        params["name"] = name
        params["googleid"] = googleid
        val parameters = JSONObject(params as Map<*, *>?)

        val url = "http://marupeace.com/goapi/login"
        val queue = Volley.newRequestQueue(thisContext)
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, parameters,
            Response.Listener {
                Log.d("Mainactivity", "Api call successful ")
                val jsonObject = it
                val data = jsonObject.getJSONObject("data")
                intentPutExtra("name", data.get("name").toString())
                signInDone(data.get("id").toString())
            }, Response.ErrorListener {
                Log.d("Mainactivity", "Api call unsuccessful "+it.toString())
            }
        ){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val headers: MutableMap<String, String> = HashMap()
                headers["X-Session-Token"] = "abcd"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }
    private fun signInDone(userid: String){
        //Toast.makeText(applicationContext,"user " + userId, Toast.LENGTH_SHORT).show()

        mainIntent.putExtra("id",userid);
        startActivity(thisContext, mainIntent, null)
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}