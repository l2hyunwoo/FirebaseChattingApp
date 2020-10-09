package com.example.firebasechatapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val DEFAULT_MSG_LENGTH_LIMIT = 1000
    private val RC_SIGN_IN = 1
    private val ANONYMOUS = "anonymous"
    private val TAG = "MainActivity"

    //Entry Point For Our App to access database
    private lateinit var mFirebaseDatabase : FirebaseDatabase
    //Refer Specific part(eg. message part) of database
    private lateinit var mMessageDatabaseReference: DatabaseReference
    private var mChildEventListener: ChildEventListener? = null
    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener
    private lateinit var mMessageAdapter: MessageAdapter

    private var mUserName = ANONYMOUS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        //mFirebaseDatabase.getReference(): Getting Reference of Root Node
        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("messages")

        mFirebaseAuth = FirebaseAuth.getInstance()

        /*
        * Take all My messages from DB Reference
        * Add EventListener that reacts db changes in real time
        * add new messages in ui
        * */

        val mMessageData = mutableListOf<MessageData>()
        mMessageAdapter = MessageAdapter(this)
        mMessageAdapter.datas = mMessageData
        rv_messages.adapter = mMessageAdapter
        mMessageAdapter.notifyDataSetChanged()

        photoPickerButton.setOnClickListener {
            // TODO: Fire an intent to show an image picker
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                sendButton.isEnabled = charSequence?.toString()!!.trim().isNotEmpty()
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        messageEditText.filters = arrayOf<InputFilter>(LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))

        sendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val newMessage = MessageData(messageEditText.text.toString(), mUserName, null)
                mMessageDatabaseReference.push().setValue(newMessage)
                messageEditText.setText("")
            }
        })



        mAuthStateListener = object :FirebaseAuth.AuthStateListener{
            // 인자로 들어간 firebaseAuth는 이미 인가 상태가 확정됨 위의 변수는 아님
            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
                val user = firebaseAuth.currentUser
                if (user != null){
                    //user가 로그인
                    //Toast.makeText(this@MainActivity, "Signed In", Toast.LENGTH_SHORT).show()
                    onSignedInInitialize(user.displayName!!)
                } else {
                    //user가 로그아웃 상태
                    onSignedOutCleanup()
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                listOf(
                                    GoogleBuilder().build(),
                                    EmailBuilder().build(),
                                )
                            )
                            .build(),
                        RC_SIGN_IN
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.sign_out_menu -> {
                AuthUI.getInstance().signOut(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
            } else if(resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }

    // Resume이 될 때 attachListener가 부착
    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    //onPause할 때 detachListener(Destroy 시)
    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
        detachDatabaseListener()
        mMessageAdapter.datas.clear()
    }


    private fun onSignedInInitialize(userName : String) {
        mUserName = userName
        attachDatabaseReadListener()
    }

    private fun onSignedOutCleanup() {
        //Unset user name, clear message list, detach listener
        mUserName = ANONYMOUS
        //왜 clear?
        // 로그인 로그아웃하면서 datas에 잔재된 데이터들이 남아 이후 로그인할 때 메시지 불러오는 데 영향줄 수 있음
        mMessageAdapter.datas.clear()
        detachDatabaseListener()
    }

    private fun attachDatabaseReadListener() {
        //onCreate는 onResume에서 계정 로그인 판별하기 이전 (AuthStateListener가 부착이 안됨)
        //인증이 제대로 안된 채 작동할 수 있음
        mChildEventListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // When New Message is added
                Log.d("Firebase", "Added")
                val message = snapshot.getValue(MessageData::class.java)
                Log.d("Firebase", "$message")
                if (message != null) {
                    mMessageAdapter.datas.add(message)
                }
                mMessageAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //Existing message gets changed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //delete
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //change position
            }

            override fun onCancelled(error: DatabaseError) {
                //don't have permission
            }

        }
        mMessageDatabaseReference.addChildEventListener(mChildEventListener!!)
    }

    private fun detachDatabaseListener(){
        //구조적으로 Sign in/Sign out 구조를 만들어 줌
        //로그인 시 Attach
        //로그아웃 시 Detach
        if(mChildEventListener != null) {
            mMessageDatabaseReference.removeEventListener(mChildEventListener!!)
        }
    }
}