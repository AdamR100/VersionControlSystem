package svcs
import java.io.File
import java.security.MessageDigest
import java.math.BigInteger
val separator = File.separator
val vcs = File("vcs")
val config = File("vcs${separator}config.txt")
val index = File("vcs${separator}index.txt")
val commits = File("vcs${separator}commits")
val log = File("vcs${separator}log.txt")
val checkFile = File("vcs${separator}checkList.txt")
fun main(args: Array<String>) {
    val instruction = """These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file."""
    vcs.mkdir()
    config.createNewFile()
    index.createNewFile()
    commits.mkdir()
    log.createNewFile()
    checkFile.createNewFile()
    if (args.isEmpty()) {
        println(instruction)
        return
    }

    when(args[0].substring(0, args[0].length)) {
        "--help" -> println(instruction)
        "checkout" -> checkout(args)
        "add" -> add(args)
        "config" -> configFun(args)
        "commit" -> commit(args)
        "log" -> logFun()
        else -> println("'${args[0]}' is not a SVCS command.")
    }
}

fun commit(args: Array<String>) {
    if(args.size == 1) {
        println("Message was not passed.")
        return
    }


    val name = config.readText()
    val string = args[1]
    if(checkFile.readText() != "status = true") {
        println("Nothing to commit.")
        checkFile.writeText("status = true")
    } else {
        log.appendText("""
$string
Author: $name
commit ${SHA256(string)}""")
        val hashFolder = File("vcs${separator}commits${separator}${SHA256(string)}")
        hashFolder.mkdir()
        index.readLines().forEach {
            hashFolder.resolve(it).createNewFile()
            hashFolder.resolve(it).writeText(File(it).readText())
        }
        println("Changes are committed.")
        checkFile.writeText("status = false")
    }
}

fun checkout(args: Array<String>) {
    if(args.size == 1) {
        println("Commit id was not passed.")
        return
    }
    if(!commits.resolve(args[1]).exists()) {
        println("Commit does not exist.")
        return
    }
    if(commits.resolve(args[1]).listFiles().isEmpty()) {
        println("Empty hash dir.")
        return
    }
    var text = mutableListOf<String>()
    for (i in commits.resolve(args[1]).listFiles()) {
        text.add(i.readText())
        File(i.name).writeText(i.readText())
    }
    for (i in commits.resolve(args[1]).listFiles()) {
        i.writeText(text[commits.resolve(args[1]).listFiles().indexOf(i)])
    }
    println("Switched to commit ${args[1]}.")

}

fun logFun(){
    if(commits.listFiles().isEmpty()) {
        println("No commits yet.")
        return
    }
    var order = ""
    log.forEachLine{ order = it + '\n' + order }
    print(order)
}

fun configFun(args: Array<String>) {
        if(args.size > 1) {
            config.writeText("${args[1]}")
    }

    if(config.readText().isBlank()) println("Please, tell me who you are.")
    else println("The username is ${config.readText()}.")
}

fun add(args: Array<String>) {
    when {
        args.size > 1 -> {
            val file = File(args[1])
            if(file.exists()) {
                index.appendText("$file\n")
                println("The file '$file' is tracked.")
                checkFile.writeText("status = true")
            } else println("Can't find '$file'.")
        }
        index.readText().isBlank() -> println("Add a file to the index.")
        else -> println("Tracked files:\n${index.readText()}")
    }
}
fun SHA256(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}
