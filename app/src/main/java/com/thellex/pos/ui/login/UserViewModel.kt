import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thellex.pos.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val context: Context) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    init {
        // Load token from DataStore on ViewModel init
        viewModelScope.launch {
            UserPreferences.getToken(context).collect { savedToken ->
                _token.value = savedToken
            }
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            UserPreferences.saveToken(context, token)
            _token.value = token
        }
    }

    fun logout() {
        viewModelScope.launch {
            UserPreferences.clearToken(context)
            _token.value = null
        }
    }
}
