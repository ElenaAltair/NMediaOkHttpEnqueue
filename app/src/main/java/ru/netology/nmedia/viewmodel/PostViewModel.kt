package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0,
    attachment = null,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAll(object : PostRepository.GetCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {

        edited.value?.let {

            repository.save(it, object : PostRepository.GetCallback<Post> {
                override fun onSuccess(post: Post) {
                    val postsN = _data.value?.posts.orEmpty()
                    _data.postValue(_data.value?.copy(posts = postsN))
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
            _postCreated.postValue(Unit)

        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val old = _data.value?.posts.orEmpty() // в переменной old сохраним список постов
        val new: List<Post> = old.onEach { post ->
            if (post.id == id) {
                if (!post.likedByMe) {
                    post.likes += 1
                    post.likedByMe = true
                } else {
                    post.likedByMe = false
                    post.likes -= 1

                }
            }
        }

        val post: Post = _data.value?.posts.orEmpty()
            .find { it.id == id } ?: return
        _data.postValue(_data.value?.copy(posts = new))

        if (post.likedByMe) {
            repository.likeById(id,
                object : PostRepository.GetCallback<Unit> {
                    override fun onSuccess(value: Unit) {
                        _data.postValue(FeedModel(posts = new))
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })

        } else {
            repository.unlikeById(id,
                object : PostRepository.GetCallback<Unit> {
                    override fun onSuccess(value: Unit) {
                        _data.postValue(FeedModel(posts = new))
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })

        }
    }

    fun removeById(id: Long) {

        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        repository.removeById(id, object : PostRepository.GetCallback<Unit> {
            override fun onSuccess(value: Unit) {
                val postsN = _data.value?.posts.orEmpty()
                _data.postValue(_data.value?.copy(posts = postsN))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        })


    }
}
