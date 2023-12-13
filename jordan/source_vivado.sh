#!/bin/bash
#Script used to initalize vivado on remote (personal machine on ISU vpn) or local (machine on campus or on artificery).
#Ran by using the command `source source_vivado.sh <remote|local>`
vivado_dir="/opt/Xilinx/Vivado/2020.1"

if [[ "$1" == "remote" ]]; then
    export LM_LICENSE_FILE=1717@io.ece.iastate.edu:27006@io.ece.iastate.edu:27008@io.ece.iastate.edu
    if [ -d "$vivado_dir" ]; then
        source  $vivado_dir/settings64.sh
    else
        echo "Error: $vivado_dir not found. Update vivado_dir to point to the correct path"
    fi
elif [[ "$1" == "local" ]]; then
    source /remote/Xilinx/2020.1/Vitis/2020.1/settings64.sh
else
    echo "Invalid option. Please choose either 'remote' or 'local'."
fi

