import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

class InstantDeserializer : JsonDeserializer<Instant> {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Instant {
        return Instant.parse(json.asString)
    }
}
