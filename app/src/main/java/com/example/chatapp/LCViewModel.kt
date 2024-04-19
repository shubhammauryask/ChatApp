package com.example.chatapp

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.chatapp.Data.CHATS
import com.example.chatapp.Data.ChatData
import com.example.chatapp.Data.ChatUser
import com.example.chatapp.Data.Event
import com.example.chatapp.Data.MESSAGE
import com.example.chatapp.Data.Message
import com.example.chatapp.Data.STATUS
import com.example.chatapp.Data.Status
import com.example.chatapp.Data.UserData
import com.example.chatapp.Data.User_Node
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {
    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    var eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    var userDate = mutableStateOf<UserData?>(null)
    var chats = mutableStateOf<List<ChatData>>(listOf())
    var chatMessages = mutableStateOf<List<Message>>(listOf())
    var inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? =
        null // reading mess everytime that came in firebase
    var status = mutableStateOf<List<Status>>(listOf())
    var inProgressStatus = mutableStateOf(false)


    init {
        val currenUser = auth.currentUser
        signIn.value = currenUser != null
        currenUser?.uid?.let {
            getUserDate(it)
        }
    }

    fun populateMessage(chaId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chaId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy {
                        it.timestamp
                    }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun dePopulateMessage() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProcess.value = true
        if (name.isEmpty() or number.isEmpty() or password.isEmpty() or email.isEmpty()) {
            handleException(customMessage = "Please Fill All the fields")
        }
        inProcess.value = true
        db.collection(User_Node).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            signIn.value = true;
                            cretaeOrUpdateProfile(name, number)
                        } else {
                            handleException(it.exception, customMessage = "SignUp failed")
                        }
                    }
            } else {
                handleException(customMessage = "User already exist with this number")
            }
        }

    }

    fun loginIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All the fields")
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = true
                    auth.currentUser?.uid?.let {
                        getUserDate(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "User Not Found")
                }
            }
        }
    }


    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            cretaeOrUpdateProfile(imageUrl = it.toString())
        }
    }

//    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
//        inProcess.value = true
//        val storageRef = storage.reference
//        val uuid = UUID.randomUUID()
//        val imageRef = storageRef.child("images/$uuid")
//        val uploadTask = imageRef.putFile(uri)
//        uploadTask.addOnSuccessListener {
//            val result = it.metadata?.reference?.downloadUrl
//            result?.addOnSuccessListener(onSuccess)
//            inProcess.value = false
//
//        }.addOnFailureListener {
//            handleException(it)
//        }
//    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener { uploadTaskSnapshot ->
            // Get the download URL directly from the upload task snapshot
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl) // Pass the download URL to the onSuccess callback
                inProcess.value = false
            }.addOnFailureListener { exception ->
                // Handle download URL retrieval failure
                handleException(exception = exception)
                inProcess.value = false
            }
        }.addOnFailureListener { exception ->
            // Handle upload failure
            handleException(exception)
            inProcess.value = false
        }
    }

//    fun cretaeOrUpdateProfile(
//        name: String? = null,
//        number: String? = null,
//        imageUrl: String? = null
//    ) {
//        val uid = auth.currentUser?.uid
//        val userData = UserData(
//            userId = uid,
//            name = name ?: userDate.value?.name,
//            number = number ?: userDate.value?.number,
//            imageUrl = imageUrl ?: userDate.value?.imageUrl,
//        )
//        uid?.let {
//            inProcess.value = true
//
//            db.collection(User_Node).document(uid).get().addOnSuccessListener {
//                if (it.exists()) {
//                    //update
//                } else {
//                    db.collection(User_Node).document(uid).set(userData)
//                    getUserDate(uid)
//                    inProcess.value = false
//                }
//            }.addOnFailureListener {
//                handleException(it, "Cannot retrieve User Data")
//            }
//
//        }
//    }
fun cretaeOrUpdateProfile(
    name: String? = null,
    number: String? = null,
    imageUrl: String? = null
) {
    val uid = auth.currentUser?.uid
    val userData = UserData(
        userId = uid,
        name = name ?: userDate.value?.name,
        number = number ?: userDate.value?.number,
        imageUrl = imageUrl ?: userDate.value?.imageUrl,
    )

    uid?.let { userId ->
        inProcess.value = true

        db.collection(User_Node).document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Update existing profile
                    db.collection(User_Node).document(userId).set(userData)
                        .addOnSuccessListener {
                            getUserDate(userId)
                            inProcess.value = false
                        }
                        .addOnFailureListener { exception ->
                            handleException(exception, "Failed to update profile")
                            inProcess.value = false
                        }
                } else {
                    // Create new profile
                    db.collection(User_Node).document(userId).set(userData)
                        .addOnSuccessListener {
                            getUserDate(userId)
                            inProcess.value = false
                        }
                        .addOnFailureListener { exception ->
                            handleException(exception, "Failed to create profile")
                            inProcess.value = false
                        }
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve User Data")
                inProcess.value = false
            }
    }
}


    private fun getUserDate(uid: String) {
        inProcess.value = true
        db.collection(User_Node).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot retrieve the user")
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userDate.value = user
                inProcess.value = false
                populateData()
                populateStatus()
            }
        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LveChat", "live Chat Exception", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMessage else customMessage

        eventMutableState.value = Event(message)
        inProcess.value = false
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userDate.value = null
        dePopulateMessage()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")

    }

    fun onAddChat(number: String) {
        if (number.isEmpty() && !number.isDigitsOnly()) {
            handleException(customMessage = "Number must have cintain digits only")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2,number", userDate.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user1.number", userDate.value?.number),
                        Filter.equalTo("user2,number", number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(User_Node).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "Number Not found shar app to other first")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userDate.value?.userId,
                                        userDate.value?.name,
                                        userDate.value?.imageUrl,
                                        userDate.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat Already exists")
                }
            }
        }
    }

    fun populateData() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userDate.value?.userId),
                Filter.equalTo("user2.userId", userDate.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }

        }
    }

    fun onSendReply(chaId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userDate.value?.userId, message, time)
        db.collection(CHATS).document(chaId).collection(MESSAGE).document().set(msg)
    }

    fun uploadSatatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(imageUri = it.toString())
        }
    }

    fun createStatus(imageUri: String) {
        val newStatus = Status(
            ChatUser(
                userDate.value?.userId,
                userDate.value?.name,
                userDate.value?.imageUrl,
                userDate.value?.number
            ),
            imageUri,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatus() {
        val  timeDelta = 24L *60 *60 *1000
        val timeCutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userDate.value?.userId),
                Filter.equalTo("user2.userId", userDate.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                val currentConnection = arrayListOf(userDate.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach { chats ->
                    if (chats.user1.userId == userDate.value?.userId) {
                        currentConnection.add(chats.user2.userId)
                    } else {
                        currentConnection.add(chats.user1.userId)
                    }
                }
                db.collection(STATUS).whereGreaterThan("timestamp",timeCutOff).whereIn("user.userId", currentConnection)
                    .addSnapshotListener { value, error ->
                        if(error != null){
                            handleException(error)
                        }
                        if(value != null){
                            status.value = value.toObjects()
                            inProgressStatus.value =false
                        }
                    }

            }
        }
    }


}