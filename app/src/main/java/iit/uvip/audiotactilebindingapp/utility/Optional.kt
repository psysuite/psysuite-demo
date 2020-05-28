package iit.uvip.audiotactilebindingapp.utility

sealed class Optional<out T> {

    data class Present<out T>(val value: T) : Optional<T>()

    object Absent : Optional<Nothing>()

    companion object
}

fun <T> T?.toOptional(): Optional<T> =
    if (this != null) Optional.Present(this)
    else Optional.Absent

fun <T> Optional<T>.orNull(): T? = when (this) {
    is Optional.Present -> value
    is Optional.Absent -> null
}

fun Optional<*>.isPresent() = this !is Optional.Absent

fun <T> Optional.Companion.absent() = Optional.Absent as Optional<T>
