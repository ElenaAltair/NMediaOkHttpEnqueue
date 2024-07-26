package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(callback: PostRepository.GetCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    val posts: List<Post> =
                        response.let { it.body?.string() ?: throw RuntimeException("body is null") }
                            .let {
                                gson.fromJson(it, typeToken.type)
                            }

                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })

    }

    override fun likeById(id: Long, callback: PostRepository.GetCallback<Unit>) {
        // формируем запрос к серверу
        val request: Request = Request.Builder()
            .post(
                gson.toJson(id).toRequestBody(jsonType)
            ) // указываем тип запроса (в этом случае запрос типа POST)
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })

    }

    override fun unlikeById(id: Long, callback: PostRepository.GetCallback<Unit>) {
        // формируем запрос к серверу
        val request: Request = Request.Builder()
            .delete(
                gson.toJson(id).toRequestBody(jsonType)
            ) // указываем тип запроса (в этом случае запрос типа POST)
            .url("${BASE_URL}/api/slow/posts/$id/likes")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })

    }

    override fun save(post: Post, callback: PostRepository.GetCallback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                    val post: Post =
                        response.let { it.body?.string() ?: throw RuntimeException("body is null") }
                            .let { gson.fromJson<Post>(it, object : TypeToken<Post>() {}.type) }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override fun removeById(id: Long, callback: PostRepository.GetCallback<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }
}
