import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thellex.pos.data.model.UserEntity
import com.thellex.pos.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val context: Context) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _authResult = MutableStateFlow<UserEntity?>(null)
    val authResult: StateFlow<UserEntity?> = _authResult

    init {
        // Load token from DataStore on ViewModel init
        viewModelScope.launch {
            UserPreferences.getToken(context).collect { savedToken ->
                _token.value = savedToken
            }
        }

        viewModelScope.launch {
            UserPreferences.getAuthResult(context).collect { result ->
                _authResult.value = result
            }
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
            UserPreferences.saveAuthResult(context, result)
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
