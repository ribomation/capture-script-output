// def patch(String template, Map context) {
//     template.replaceAll(/@(\w+)@/) {_,key -> context[key]}
// }

def call(Map params = [:]) {
    def name = params.name ?: 'run-script'
    def cmd  = params.cmd  ?: 'ls -lhFA .'
    def ctx  = [NAME:name, CMD:cmd]

    def tmpl = libraryResource 'ribomation/run_script/script.tmpl.sh' 
    if (!tmpl) { throw new RuntimeException('template file was empty') }

    def scriptText = tmpl.replaceAll(/@(\w+)@/) {_,key -> ctx[key]}
    echo '--script--'
    echo scriptText
    echo '--end script--'
    
    def dir = "./TMP.${name}.${System.nanoTime()}"
    writeFile file:"${dir}/cmd.sh", text:scriptText
    writeFile file:"${dir}/stdout.txt", text:''
    writeFile file:"${dir}/stderr.txt", text:''
    writeFile file:"${dir}/exit.txt", text:''
    writeFile file:"${dir}/README.txt", text:'Temp-dir created for invocation of runScript'
    
    sh "chmod a+x ${dir}/cmd.sh"
    sh "ls -lhFA ${dir}"
    
    def SH_CMD = "${dir}/cmd.sh 2>${dir}/stderr.txt 1>${dir}/stdout.txt; echo \$? >${dir}/exit.txt"
    echo 'SH_CMD: "' + SH_CMD + '"'
    sh SH_CMD

    def result  = [:]
    result.out  = readFile "${dir}/stdout.txt"
    result.err  = readFile "${dir}/stderr.txt"
    result.exit = readFile "${dir}/exit.txt"
    result.ok   = result.exit == '0' && result.err.trim().isEmpty()

    return result
}
