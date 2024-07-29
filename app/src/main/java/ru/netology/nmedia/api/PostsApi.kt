package ru.netology.nmedia.api

import com.google.firebase.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    //if (BuildConfig.DEBUG) { // пришлось закоментировать, иначе не писались логги okhttpclient
        // устанавливаем для стадии разработки уровень логирования BODY
        level = HttpLoggingInterceptor.Level.BODY
        //setLevel(HttpLoggingInterceptor.Level.BODY)
    //}
}

// создадим клиента OkHttp
private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(logging) // и передадим туда logging
    .build()

// создадим клиент retrofit
// retrofit - это клиент, который знает куда обращаться
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl("http://10.0.2.2:9999/api/slow/")// задаём базовый адрес
    .client(client) // чтобы создавались логи на стадии разработки
    .build()

interface PostsApiService { // интерфейс(PostsApiService), в котором будут перечислены методы для обращения
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @GET("posts/{id}")
    fun getById(@Path("id") id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun dislikeById(@Path("id") id: Long): Call<Post>
}

object PostsApi {
    val service: PostsApiService by lazy {
        retrofit.create(PostsApiService::class.java) // retrofit.create() сюда передаём интерфейс(PostsApiService), в котором будут перечислены методы для обращения
    }
}