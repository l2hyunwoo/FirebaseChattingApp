package com.example.firebasechatapp

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val DEFAULT_MSG_LENGTH_LIMIT = 1000
    private val ANONYMOUS = "anonymous"
    private val TAG = "MainActivity"

    private var mUserName = ANONYMOUS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mMessageData = mutableListOf<MessageData>()
        val mMessageAdapter = MessageAdapter(this)
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

        sendButton.setOnClickListener{
            messageEditText.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}