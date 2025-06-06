import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thellex.pos.data.model.UserEntity
import com.thellex.pos.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserViewModel(application: Context):ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _authResult = MutableStateFlow<UserEntity?>(null)
    val authResult: StateFlow<UserEntity?> = _authResult.asStateFlow()

    init {
        observeToken()
        observeAuthResult()
    }

    private fun observeToken() {
        viewModelScope.launch {
            UserPreferences.getToken(context)
                .catch { emit(null) }
                .collect { _token.value = it }
        }
    }

    private fun observeAuthResult() {
        viewModelScope.launch {
            UserPreferences.getAuthResult(context)
                .catch { emit(null) }
                .collect { _authResult.value = it }
        }
    }

    fun saveToken(token: String?) {
        viewModelScope.launch {
            if (token != null) {
                UserPreferences.saveToken(context, token)
            } else {
                UserPreferences.clearToken(context)
            }
            _token.value = token
        }
    }

    fun saveAuthResult(result: UserEntity) {
        viewModelScope.launch {
            UserPreferences.saveToken(context, result.token)
            UserPreferences.saveAuthResultAsync(context, result)
            _token.value = result.token
            _authResult.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            UserPreferences.clearToken(context)
            UserPreferences.clearAuthResult(context)
            _token.value = null
            _authResult.value = null
        }
    }
}
