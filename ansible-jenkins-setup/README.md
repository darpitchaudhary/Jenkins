# Ansible commands

# Setup Infra
- ansible-playbook -i inventory jenkins_networking_setup.yml -vvv

# Teardown Infra 
- ansible-playbook -i inventory jenkins_network_teardown.yml -vvv --extra-vars "Name=app tagValue=jenkins"

#Demo
