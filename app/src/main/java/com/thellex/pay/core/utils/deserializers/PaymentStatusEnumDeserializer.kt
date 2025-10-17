import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.thellex.pay.data.model.PaymentStatusEnum
import java.lang.reflect.Type

class PaymentStatusEnumDeserializer : JsonDeserializer<PaymentStatusEnum?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PaymentStatusEnum? {
        val value = json?.asString ?: return null
        return PaymentStatusEnum.fromValue(value)
    }
}