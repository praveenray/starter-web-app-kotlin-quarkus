package my.starter.libs

sealed class OpResult<out Ok, out Err>

data class Ok<out Ok>(val value: Ok? = null): OpResult<Ok, Nothing>()
data class Err<out Err>(val error: Err): OpResult<Nothing, Err>()
