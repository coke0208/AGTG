package com.example.test


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.test.databinding.ActivityMainBinding
import com.example.test.databinding.FragmentAddBinding
import com.example.test.productinfo.FirebaseRF
import com.example.test.productinfo.ProductDB
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONException
import org.json.JSONObject


class AddFragment: Fragment() {

    private lateinit var viewModel: TodoViewModel
    private lateinit var binding: FragmentAddBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. View Model 설정
        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()) .get(
            TodoViewModel::class.java)
        // 2. View Binding 설정
        binding = FragmentAddBinding.inflate(inflater, container, false)
        // 3. return Fragment Layout View
        return binding.root
    }

    // Add Fragment -> Home Fragment intent && viewmodel addTask 원활하게 호출하기 위해
    // onStart (Activity 만들어진 후, 사용자에게 보여지는 시점) 에서 Add Button에 onClickListener 추가
    override fun onStart() {
        super.onStart()
        binding.btnSave.setOnClickListener{
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()

            // 새로운 item 생성해서 viewmodel의 addTask 호출
            viewModel.addTask(Todo(title, content, false))

            // Add Fragment -> Home Fragment로 intent
            val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, HomeFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }
    class MainActivity : AppCompatActivity() {
        //view Objects
        private var btnSave: Button? = null
        private var title: TextView? = null
        private var content: TextView? = null
        private var info: TextView? = null
        private lateinit var binding: FragmentAddBinding

        private var mDatabase: DatabaseReference? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = FragmentAddBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //setContentView(R.layout.act)
            btnSave = binding.btnSave
            mDatabase = FirebaseDatabase.getInstance().getReference()
            btnSave!!.setOnClickListener {
                val title = binding.etTitle.text.toString()
                val content = binding.etContent.toString()
                val info = binding.info.text.toString()

                val productdb=ProductDB(title,content)
                FirebaseRF.productdb.child(title).setValue(productdb)
                Toast.makeText(this,"저장성공",Toast.LENGTH_LONG).show()
            }


        }

        //Getting the scan results
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            val result:IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                //qrcode 가 없으면
                if (result.contents == null) {
                    Toast.makeText(this@MainActivity, "취소!", Toast.LENGTH_SHORT).show()
                } else {
                    //qrcode 결과가 있으면
                    Toast.makeText(this@MainActivity, "스캔완료!", Toast.LENGTH_SHORT).show()
                    try {
                        //data를 json으로 변환
                        val obj = JSONObject(result.contents)
                        binding.etTitle.text= obj.getString("name")
                        content!!.text = obj.getString("addres")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        //Toast.makeText(MainActivity.this, result.getContents(), Toast.LENGTH_LONG).show();
                        info?.setText(result.contents)
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

        /*private fun writeNewUser(
            userId: Int,
            userName: String,
            userPassword: String,
            userNumber: String
        ) {
            val user = user(userName, userPassword, userNumber)
            mDatabase!!.child("users").child(userId.toString()).setValue(user)
                .addOnSuccessListener { // Write was successful!
                    Toast.makeText(this@MainActivity, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { // Write failed
                    Toast.makeText(this@MainActivity, "저장을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }*/

       /* private fun readUser() {
            mDatabase!!.child("users").child("1")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                        // Get Post object and use the values to update the UI
                        if (dataSnapshot.getValue(user::class.java) != null) {
                            val post: user? = dataSnapshot.getValue(user::class.java)
                            Log.w("FireBaseData", "getData" + post.toString())
                        } else {
                            Toast.makeText(this@MainActivity, "데이터 없음...", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    override fun onCancelled(@NonNull databaseError: DatabaseError) {
                        // Getting Post failed, log a message
                        Log.w("FireBaseData", "loadPost:onCancelled", databaseError.toException())
                    }
                })
        }*/
    }


}