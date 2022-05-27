package com.shiva.learning.kotlinmessenger

import com.shiva.learning.kotlinmessenger.support.Progress
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shiva.learning.kotlinmessenger.databinding.ActivityRegisterBinding
import com.shiva.learning.kotlinmessenger.support.Util
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedUri: Uri? = null

    private var progress: Progress? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener{
            performRegister()
        }

        binding.txtBackToLogin.setOnClickListener{
           finish()
        }

        binding.btnProfilePictSelect.setOnClickListener{
            loadImage.launch("image/*")
        }
    }

    private val loadImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        if(uri==null) return@registerForActivityResult
        selectedUri = uri;
        Log.i(TAG, "Profile photo selected")
        val capturedImage = Util.getCapturedImage(this, uri)
        binding.imgProfilePict.setImageBitmap(capturedImage)
        binding.btnProfilePictSelect.alpha = 0f
    }


    private fun performRegister() {
        val password: String = binding.txtPassword.text.toString()
        val email: String = binding.txtEmail.text.toString()

        if(email.isEmpty() || password.isEmpty() || selectedUri==null){
            showToast("Username or password or profile pict is empty")
            return
        }
        setProgressDialogVisibility(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
//                    val user = auth.currentUser
                    uploadImageToFireBaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", it.exception)
                    Toast.makeText(baseContext, "Create user failed. ${it.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    setProgressDialogVisibility(false)
                }

            }
    }

    private fun uploadImageToFireBaseStorage() {
        if(selectedUri==null)return
        else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            progress?.setProgressMessage(R.string.uploading_image)
            // !! unwrapping it
            ref.putFile(selectedUri!!)
                .addOnSuccessListener {
                    val imageUrl : String? = it.metadata?.path
                    Log.d(TAG, "uploadImageToFireBaseStorage:success")
                    val username = binding.txtUsername.text.toString()
                    addUserInformationToFirebaseDB(username, imageUrl ?: "")
                }
                .addOnFailureListener{
                    Log.d(TAG, "uploadImageToFireBaseStorage:failed")
                    setProgressDialogVisibility(false)
                }
        }
    }

    private fun addUserInformationToFirebaseDB(username:String, profileImageUrl:String) {
        var uid = FirebaseAuth.getInstance().uid;
        var ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        var user = User(uid, username, profileImageUrl);
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "addUserInformationToFirebaseDB:success")
                showToast("User registered successfully")
                setProgressDialogVisibility(false)
                finish()
            }
            .addOnFailureListener{
                Log.e(TAG, it.message.toString())
                setProgressDialogVisibility(false)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setProgressDialogVisibility(visible: Boolean) {
        if (visible) progress = Progress(this, R.string.Registering_User, cancelable = false)
        progress?.apply { if (visible) show() else dismiss() }
    }
}

class User(val uid: String?, val username: String, val profileImageUrl: String)
