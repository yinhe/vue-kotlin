import org.musyozoku.vuekt.*
import kotlin.js.Json

external interface ModelComputed : Json {
    var message: String
    val reversedMessage: String

    var firstName: String
    var lastName: String
    var fullName: String
}

external interface ModelWatch : Json {
    var question: String
    var answer: String
    fun getAnswer(): Unit
}

fun main(args: Array<String>) {
    val vm = Vue {
        el = "#example"
        data = Json<ModelComputed> {
            message = "Hello"
            firstName = "Foo"
            lastName = "Bar"
        }
        computed = Json {
            set("reversedMessage") {
                val self = thisAs<ModelComputed>()
                self.message.split("").reversed().joinToString("")
            }
            set("fullName", Json<Accessor<String>> {
                get = {
                    val self = thisAs<ModelComputed>()
                    "${self.firstName} ${self.lastName}"
                }
                set = {
                    newValue ->
                    val (firstName, lastName) = newValue.split(" ")
                    val self = thisAs<ModelComputed>()
                    self.firstName = firstName
                    self.lastName = lastName
                }
            })
        }
    }
    val model = vm.proxyOf<ModelComputed>()
    println(model.reversedMessage)
    model.message = "Goodbye"
    println(model.reversedMessage)
    println(model.fullName)
    model.fullName = "John Doe"
    println(model.fullName)
    println(model.firstName)
    println(model.lastName)

    Vue {
        el = "#watch-example"
        data = Json<ModelWatch> {
            question = ""
            answer = "I cannot give you an answer until you ask a question!"
        }
        watch = Json {
            set("question") {
                val self = thisAs<ModelWatch>()
                self.answer = "Waiting for you to stop typing..."
                self.getAnswer()
            }
        }
        methods = Json {
            set("getAnswer", lodash.debounce(
                    {
                        val self = thisAs<ModelWatch>()
                        if (self.question.indexOf("?") == -1) {
                            self.answer = "Questions usually contain a question mark. ;-)"
                        } else {
                            self.answer = "Thinking..."
                            axios.get("https://yesno.wtf/api")
                                    .then({
                                        response: dynamic ->
                                        self.answer = lodash.capitalize(response.data.answer)
                                        null
                                    })
                                    .catch({
                                        error: String ->
                                        self.answer = "Error! Could not reach the API. " + error
                                        null
                                    })
                        }
                    },
                    500
            ))
        }
    }
}