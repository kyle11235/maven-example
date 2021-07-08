node {
    stage("Run Ansible Playbook"){
        sh "cd /tmp/data/ansible_playbook && ansible-playbook replace.yaml"
    }
}
