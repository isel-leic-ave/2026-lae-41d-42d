function checkMembers(obj) {
    console.log("############################")
    console.log(obj.constructor.name)
    console.log("----------------------------")
    Object
        .keys(obj.constructor.prototype)
        .forEach(name => {
            if(typeof obj[name] == "function" )
                console.log("FUNC "  + name)
            else 
                console.log("Prop "  + name)
        })
}

function checkAndCallMethods(obj) {
    console.log("############################")
    console.log(obj.constructor.name)
    console.log("----------------------------")
    Object
        .keys(obj.constructor.prototype)
        .filter(name => typeof obj[name] == "function" && obj[name].length == 0)
        .map(name => obj[name])
        .forEach(func => {
            console.log(func.name + "() ----> " + func.call(obj))
        })
}

checkAndCallMethods(new URL("https://github.com"))
checkAndCallMethods(new URL("https://isel.pt"))

checkAndCallMethods(performance)
