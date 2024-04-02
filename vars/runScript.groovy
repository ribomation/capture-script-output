
@NonCPS
String patch(String template, Map context) {
    String pattern      = /@(\w+)@/
    Closure replacement = {_,key -> context[key]}
    String scriptText   = template.replaceAll(pattern, replacement) 
    return scriptText
}

def call(Map params = [:]) {
    def context  = [
        NAME: params.name ?: 'run-script', 
        CMD : params.cmd  ?: 'ls -lhFA .'
        ]
    def template = '''
        #!/usr/bin/env bash
        set -e            #stop at first error (non-zero exit code)
        set -u            #stop if using undef variable
        set -x            #echo all commands
        set -o pipefail   #set exit code to first failed cmd in a pipe

        TS=`date --iso-8601=ns`
        echo "timestamp: $TS"
        echo "directory: $PWD"
        echo "script   : @NAME@"
        @CMD@
        '''.stripIndent().trim()
    def scriptText = patch(template, context)
    
    def dir = "./TMP.${name}.${System.nanoTime()}"
    writeFile file:"${dir}/cmd.sh",     text:scriptText
    writeFile file:"${dir}/stdout.txt", text:''
    writeFile file:"${dir}/stderr.txt", text:''
    writeFile file:"${dir}/exit.txt",   text:''
    writeFile file:"${dir}/README.txt", text:'Temp-dir for runScript'
    
    sh "chmod a+x ${dir}/cmd.sh"
    def cmdLine = "${dir}/cmd.sh 2>${dir}/stderr.txt 1>${dir}/stdout.txt; echo \$? >${dir}/exit.txt"
    sh cmdLine

    def result  = [:]
    result.out  = readFile "${dir}/stdout.txt"
    result.err  = readFile "${dir}/stderr.txt"
    result.exit = readFile "${dir}/exit.txt"
    result.ok   = (result.exit.toString().trim() == '0')

    return result
}
