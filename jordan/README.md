# Chipyard FPGA Development

## Description and Project Usage

This repository contains a submodule for [Chipyard](https://github.com/ucb-bar/chipyard) and a directory titled `fpga-updates` with files that update Chipyard. This setup allows for development on Chipyard components while still tracking changes to those updates.

Using this project is slightly cumbersome, but necessary to avoid submodule tracking issues with the default Chipyard repository. With this in mind, everytime a new file of interest is created or an exisiting file in Chipyard is iterated upon, that file will have to be added to the `fpga-updates` directory. Add all pathing information that would be needed from having your working directory be the same as the top-level Chipyard path. 

While adding these files may be tedious, once added and links are created, further iteration will be easy. In addition, updating Chipyard should be as straightforward as running `git submodule update chipyard/`.

### Motivation for repository structure
While there may be better alternative to this repository than the linking approach, this project was structured this way to avoid issues with tracking submodules. Chipyard itself could have been [forked](https://docs.github.com/en/get-started/quickstart/fork-a-repo) and [rebased](https://stackoverflow.com/questions/24255519/how-to-use-the-forked-repo-and-still-track-updates-from-the-original-source), or an [upstream link](https://stackoverflow.com/questions/29306032/fork-subdirectory-of-repo-as-a-different-repo-in-github) could have been created, but the issue of [tracking internal submodule updates](https://stackoverflow.com/questions/42116867/github-how-to-fork-inside-a-repository-into-an-submodule) still persists. This is particulary important for this repository as it edits the submodules `fpga-shells` and `boom`. 

There has been [discussion](https://forum.gitlab.com/t/how-can-i-fork-a-project-and-its-submodules/32479) on this issue that revolves around creating multiple forks and having a separate repository point to those forks. Others suggest [removing git information from submodules](https://stackoverflow.com/questions/47008290/git-how-to-make-outer-repository-and-embedded-repository-work-as-common-standal) to allow for updates to them to be tracked; however, as a result of doing that, repositories can become bloated. In addition, with multiple of these approaches, because submodules are being tracked internal to Chipyard in some manner, building the Chipyard project becomes overally complicated.

> Well, over all it sounds complicated the more I think about it...modifying/removing submodules is really not fun btw.  
>    
>-Michael (GitLab Team)

As a result, this structure is currently the best approach to get this project to where it needs to be.

## Setup

### Prerequisites

To use this repository, you need to have Chipyard set up on your machine. If you already have Chipyard built and working, skip to the link step. However, if Chipyard has not been installed or built, the following steps must be taken:

**Install the Miniforge installer for Conda. Use the Miniforge installer for Conda by following the instructions [here](https://github.com/conda-forge/miniforge/#downloading-the-installer-as-part-of-a-ci-pipeline).**

1. Run the following command to download the Miniforge3 installer:
    ```
    wget -O Miniforge3.sh "https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-$(uname)-$(uname -m).sh"
    ```
2. Run the following command to install Miniforge3:
    ```
    bash Miniforge3.sh -b -p "${HOME}/miniforge3"
    ```
3. Run the following command to activate Conda (it is recommended to add the following line to your `~/.bashrc` file):
    ```
    source "${HOME}/miniforge3/etc/profile.d/conda.sh"
    ```
4. Run the following command to activate the base environment:
    ```
    conda activate base
    ```

**Build Chipyard with riscv-tools.**

5. Run the following command to initialize the chipyard submodule.
    ```
    git submodule update --init chipyard/
    ```
6. Follow the Chipyard instructions by going [here](https://chipyard.readthedocs.io/en/stable/Chipyard-Basics/Initial-Repo-Setup.html#default-requirements-installation) and complete the build setup.
   - The `-s 9` option can be added to avoid `guestmount` dependencies on `artificery`.

### Link Chipyard build to master directory

7. Use the `create_links.sh` script to update the files in the submodule. The script will recursively search for all files in the master directory and create a symbolic link in the target directory for each file--replacing files that already exist, creating the parent directories as necessary, and printing the full path of each file being linked.
    ```
    ./create_links.sh ../path/to/master/directory ../path/to/target/directory
    ```
    Note: Replace `../path/to/master/directory` with the path to the `fpga-updates` directory containing the tracked files, and replace `../path/to/target/directory` with the path to the larger chipyard directory containing the corresponding files. The master directory could be specified in more detail, such as `fpga-updates/generators` to only link certain directories; however, make sure to update your target directory to match that working directory in this example `{chipyard_path}/generators`.

## Building a Bitstream
Because this project utilizes the Chipyard framework, the instructions for the prototyping flow can be followed via their documentation [here](https://chipyard.readthedocs.io/en/stable/Prototyping/index.html). Note this project builds on Chipyard and Chipyard's version can be changed, so the procedure documented by the Chipyard group should be the main reference for this flow. However, the current steps (mostly copied from the current Chipyard documenation) will be outlined below for the ZCU106 flow.

**1. **Sources and Submodule Setup****

All prototyping-related collateral and sources are located in the `fpga` top-level Chipyard directory. This can be initialized with the following command.

```
# in the chipyard top level folder
./scripts/init-fpga.sh
```

Note that the ZCU106 flow requires the updates in `fpga-updates` to be linked in these directories, so after initialization of `fpga`, make sure to do so.

```
./create_links.sh fpga-updates/fpga chipyard/fpga
```

**2. **Generating a bitstream****

With the FPGA submodules initialized and the ZCU106 implementation linked to Chipyard, a bitstream can be generated. To generate a bitstream, Vivado needs to be on the targeted system. A script, `source_vivado.sh`, was implemented to setup Iowa State's Vivado environment whether the framework is being run locally or remotely on artificery. It is in the top directory and ran as so:
```
source source_vivado.sh <remote|local>
```
If you are running on a personal machine, you will still have to have Vivado installed and be connected to Iowa State's VPN.

With Vivado setup, the following command can be used to generate a bitstream for the ZCU106 in the `chipyard/fpga` directory (note that `chipyard/env.sh` will have to be sourced as normally when using the Chipyard framework):
```
make SUB_PROJECT=zcu106 CONFIG=BoomZCU106Config bitstream
```

This  target is defined in `fpga/Makefile` and targets a configuration defined in  `fpga/src/main/scala/zcu106/Configs.scala`. New configurations can be added to target different BOOM configs, FPGA frequencies, or FPGA implementations with different peripherals. This command can be customized in several ways and the options can be seen here:
```
make SBT_PROJECT=... MODEL=... VLOG_MODEL=... MODEL_PACKAGE=... CONFIG=... CONFIG_PACKAGE=... GENERATOR_PACKAGE=... TB=... TOP=... BOARD=... FPGA_BRAND=... bitstream

# or for the default

make SUB_PROJECT=<sub_project> bitstream
```

## Running a Design on the ZCU106

### Basic ZCU106 design
The default Xilinx ZCU106 harness, built above, is derived from Chipyard's VCU118 harness and setup to have UART, a SPI SDCard, and DDR backing memory. This allows it to run RISC-V Linux from an SDCard while piping the terminal over UART to the host machine (the machine connected to the fpga). To extend this design, you can create your own Chipyard configuration and add the `WithZCU106Tweaks` located in `fpga/src/main/scala/zcu106/Configs.scala`. Adding this config fragment will enable and connect the UART, SPI SDCard, and DDR backing memory to your Chipyard design/config. Other configurations such as a tethered-serial-interface (TSI) based design exist but are a WIP.

The basis for a ZCU106 design revolves around creating a special test harness to connect the external IOs to your Chipyard design. This is done with the `ZCU106TestHarness` in the basic default ZCU106 FPGA target. The `ZCU106TestHarness` (located in `fpga/src/main/scala/zcu106/TestHarness.scala`) uses `Overlays` that connect to the ZCU106 external IOs. Generally, the `Overlays` take an IO from the `ChipTop` (labeled as `topDesign` in the file) when “placed” and connect it to the external IO and generate necessary Vivado collateral.

### Running Linux on ZCU106 designs
As mentioned above, the default ZCU106 harness is setup with a UART and a SPI SDCard. These are utilized to both interact with the DUT (with the UART) and load in Linux (with the SDCard). The following steps describe how to build and run buildroot Linux on the prototype platform.

**1. **Building Linux with FireMarshal****

Due to [issues](https://groups.google.com/g/chipyard/c/islBKMgalRQ) with the current FireMarshal/Linux build at the time of writing, building the Linux image will be avoided by using pre-build images contained in `firemarshal-images`. If there is a need for experimenting with the image itself, `artificery` cannot be used due to `guestmount` dependencies. However, a personal machine with this utility installed can be setup by doing the following.

1. Clone [Chipyard](https://github.com/ucb-bar/chipyard)

2. Check out Chipyard's [`9443466` commit](https://github.com/ucb-bar/chipyard/tree/9443466fecea1fe7ead68911f78a96ffcb20754a).

3. Do the setup according to the [1.6.1 documentation](https://chipyard.readthedocs.io/en/1.6.1/Chipyard-Basics/Initial-Repo-Setup.html)

4. Traverse into the `software/firemarshal/` directory and check out the [`5e5fc2e` commit](https://github.com/firesim/FireMarshal/tree/5e5fc2ed8795e653ca3b5f804d9be04036462427).

5. Build the flattened image following the [1.6.1 documentation](https://chipyard.readthedocs.io/en/1.6.1/Prototyping/VCU118.html#running-linux-on-vcu118-designs)

If Chipyard's current FireMarshal and Linux environments want to be investigated follow their documentation [here](https://chipyard.readthedocs.io/en/1.6.1/Prototyping/VCU118.html#building-linux-with-firemarshal).

**2. **Setting up the SDCard****

These instructions assume that you have a spare uSDCard that can be loaded with Linux and other files using two partitions. The 1st partition will be used to store the Linux binary (created with FireMarshal or other means) while the 2nd partition will store a file system that can be accessed from the DUT. Additionally, these instructions assume you are using Linux with `sudo` privileges and `gdisk`, but you can follow a similar set of steps on Mac (using `gpt` or another similar program).

1. Wipe the GPT on the card using `gdisk`. Use the _z_ command from the expert menu (opened with ‘x’, closed with ‘m’) to zap everything. For rest of these instructions, the SDCard path is assumed to be `/dev/sdc` (replace this with the path to your SDCard).
    ```
    sudo gdisk /dev/sdc
    ```

2. Create the new GPT with _o_. Click yes on all the prompts.

3. The ZCU106 bootrom assumes that the Linux binary to load into memory will be located on sector 34 of the SDCard. Change the default partition alignment to 1 so you can write to sector 34. Do this with the _l_ command from the expert menu (opened with ‘x’, closed with ‘m’).

4. Create a 512MiB partition to store the Linux binary (this can be smaller but it must be larger than the size of the Linux binary). Use _n_, partion number 1 and select sector 34, with size +1048576 (corresponding to 512MiB). For the type, search for the apfs type and use the hex number given.

5. Create a second partition to store any other files with the rest of the SDCard. Use _n_ and use the defaults for partition number, starting sector and overall size (expand the 2nd partition to the rest of the SDCard space). For the type, search for the hfs and use the hex number given.

6. Write the changes using _w_.

7. Setup the filesystem on the 2nd partition. Note that the `/dev/sdc2` points to the 2nd partition. Use the following command:
    ```
    sudo mkfs.hfsplus -v "PrototypeData" /dev/sdc2
    ```

**3. **Transfer and Run Linux from the SDCard****

After you have a Linux boot binary and the SDCard is setup properly (1st partition at sector 34), you can transfer the binary to the 1st SDCard partition. In this example, the pre-generated `br-base-bin-nodisk-flat` found in `firemarshal_images` is loaded using `dd`. Note that `sdc1` points to the 1st partition (remember to change the `sdc` to your own SDCard path).
```
sudo dd if=firemarshal_images/br-base-bin-nodisk-flat of=/dev/sdc1
```

If you want to add files to the 2nd partition, you can also do this now by mounting the drive and transferring files.
```
sudo mount -t hfsplus -o force,rw /dev/sdc2 /mnt
```

Remember that this implementation utilizes Linux, so baremetal programs are not required when running them from the SDCard. For example a program can be simply compilied and the binary's dissasembly can be outputed like so.
```
riscv64-unknown-elf-gcc -o condBranchMispred condBranchMispred.c 

riscv64-unknown-elf-objdump -d condBranchMispred
```

After loading the SDCard with Linux and potentially other files, you can program the FPGA and plug in the SDCard. To interact with Linux via the UART console, you can connect to the serial port (in this case called `ttyUSB2`) using something like `picocom`. 
```
picocom /dev/ttyUSB2 --baud 115200

```

To assist in finding the correct USB target, a script in the top directory, `find_usb.sh`, was added. The script will print out the path and description of each USB device, the ZCU106 uses a CP2108 Quad USB to UART controller and the PL-side UART is the second port (zero indexed).
```
./find_usb.sh

/dev/ttyUSB0 - Silicon_Labs_CP2108_Quad_USB_to_UART_Bridge_Controller_40514E02BB6302B611E72B7C015B45F
/dev/ttyUSB3 - Silicon_Labs_CP2108_Quad_USB_to_UART_Bridge_Controller_40514E02BB6302B611E72B7C015B45F
/dev/ttyUSB1 - Silicon_Labs_CP2108_Quad_USB_to_UART_Bridge_Controller_40514E02BB6302B611E72B7C015B45F
/dev/ttyUSB2 - Silicon_Labs_CP2108_Quad_USB_to_UART_Bridge_Controller_40514E02BB6302B611E72B7C015B45F

```

Once connected, you should see the binary being loaded as well as Linux output (in some cases you might need to reset the DUT). Note that this implementation utilizes a PMOD connected SDCard and does not interface with the exisiting PS-side SDCard adapter on the ZCU106. For the binary to be loaded, an [adapter](https://www.adafruit.com/product/254) is needed and will have to be configured to connect to the ZCU106's J55 header in conjunction with the fpga-shell implementation, `SDIOZCU106PlacedOverlay`, located in `fpga-updates/fpga/fpga-shells/src/main/scala/xilinx/ZCU106NewShell`. Page 83 of the [ZCU106 Board User Guide](https://docs.xilinx.com/v/u/en-US/ug1244-zcu106-eval-bd) shows the PMOD connectors and the pins used in `SDIOZCU106PlacedOverlay`. The board itself has markings on the PCB to denote the pins for the PMOD GPIO Headers shown by callout 21 of Figure 2-1 on page 13 of the user guide.

Once the bitstream is programmed and the SDCard is programmed/connected, the following is expected.
```
INIT
CMD0
CMD8
ACMD41
CMD58
CMD16
CMD18
LOADING 0x01e00000B PAYLOAD
LOADING  
BOOT

...

OpenSBI v0.8
  ____                    _____ ____ _____
/  __  \                 / ____|  _ \_   _|
| |  | |_ __   ___ _ __ | (___ | |_) || |
| |  | | '_ \ / _ \ '_ \ \___ \|  _ < | |
| |__| | |_) |  __/ | | |____) | |_) || |_
\ ____/| .__/ \___|_| |_|_____/|____/_____|
       | |
       |_|


Platform Name       : freechips,rocketchip-unknown
Platform Features   : timer,mfdeleg
Platform HART Count : 1
Boot HART ID        : 0
Boot HART ISA       : rv64imafdcsu
BOOT HART Features  : pmp,scounteren,mcounteren
BOOT HART PMP Count : 16
Firmware Base       : 0x80000000
Firmware Size       : 96 KB

...

```

Once the boot up sequence is finished, sign in as ‘root’ with password ‘fpga’. Then, the second partition, containing any additional files, can be mounted.
```
mount -t hfsplus /dev/mmcblk0p2 /mnt
```

## ZCU106 TSI Implementation Instructions
An additional harness for the ZCU106 is being developed that uses a TSI-over-UART adapter to bringup the FPGA. This implementation was based on Chipyard's Arty100T design. A user can connect to the ZCU106 target using a special `uart_tsi` program that opens a UART TTY. The interface for the `uart_tsi` program provides unique functionality that is useful for bringing up test chips. This implementation, in theory, allows for the rapid deployment of baremetal applications over UART--ideal for fuzzing. Currently the build does not function, but the flow for it would be as follows.

To build the design, run:

```
cd fpga/
make SUB_PROJECT=zcu106tsi CONFIG=BoomZCU106TSIConfig
```

To build the UART-based frontend server, run:

```
cd generators/testchipip/uart_tsi
make
```

After programming the bitstream, and connecting the ZCU106's UART to a host PC via the USB cable, the uart_tsi program can be run to interact with the target.

Running a program:

```
./uart_tsi +tty=/dev/ttyUSBX dhrystone.riscv
```

Probe an address on the target system:

```
./uart_tsi +tty=/dev/ttyUSBX +init_read=0x10040 none
```

Write some address before running a program:
```
./uart_tsi +tty=/dev/ttyUSBX +init_write=0x80000000:0xdeadbeef none
```

Self-check that binary loading proceeded correctly:
```
./uart_tsi +tty=/dev/ttyUSBX +selfcheck dhrystone.riscv
```

Run a design at a higher baud rate than default (For example, if `CONFIG=UART921600RocketZCU106TSIConfig` were built):
```
./uart_tsi +tty=/dev/ttyUSBX +baudrate=921600 dhrystone.riscv
```

When testing this implementation using the RocketChip core on the Arty100T, it was found that reprogramming the board between running binaries would prevent the FPGA from stalling. If this implementation is persued for fuzzing purposes, the fuzzing round would have to reprogram the board prior to sending a new binary.

## Speculative Branch Monitor and dontTouch
To facilitate the analysis of a design’s susceptibility to speculative attacks, a branch monitor was added to the BOOM processor. It plays a vital role in identifying mispredicted branches, keeping track of important meta-data, and providing valuable insights into the performance of the processor. The implementation can be found here: `fpga=updates/generators/boom/src/main/scala/exu/rob.scala`. 

The monitor itself, comprises of _n_ entries, where _n_ represents the maximum inflight branch count of the processor, which is configurable in BOOM. When triggered by a specific instruction, the monitor starts a simple up counter to keep track of the time before a branch is detected--corresponding to the setup time required for a misprediction. Once a branch is detected, it is added as an entry to the monitor. Each entry has a counter that records how long the corresponding branch remains in the reorder buffer, providing the speculative window size associated with the branch. In addition to the counter, each entry in the monitor stores other important meta-data related to the corresponding branch. This information, listed below, is critical in identifying mispredictions and recording the fuzzing-related metrics.

- Micro Op of the branch to identify what instruction caused the misprediction

- The branch's index in the ROB to detect mispredictions

- A valid bit to determine if an entry was resolved without being mispredicted

- The cycle the branch entered the ROB copied from the trigger counter’s value

By tracking this meta-data and the time taken for a branch to execute, the monitor can aid in analyzing the susceptibility of the processor to speculative attacks. Currently, when running a design on an FPGA, the branch monitor can only be observed via an integrated logic analyzer (ILA). However, ideally, the information from a mispredict taken would be exported out of the monitor into a user-readable queue. With that structure in place, the user would be able to report the maximum speculative window size of a processor for a certain program, the setup required for that transient execution, and would be able to track the frequency of mispredictions by comparing the setup values of each queue reported misprediction.

To add an ILA to a design, Chipyard recommends opening up the post synthesis checkpoint located in the build directory for your design in Vivado (it should be labeled `post_synth.dcp`). Then using Vivado, add ILAs (and other debugging tools) for your design (search online for more information on how to add an ILA). This can be done by modifying the post synthesis checkpoint, saving it, and running `make ... debug-bitstream`. This will create a new bitstream called `top.bit` in a folder named `generated-src/<LONG_NAME>/debug_obj/`. For example, running the bitstream build for an added ILA for a BOOM config:
```
make SUB_PROJECT=zcu106 CONFIG=BoomZCU106Config debug-bitstream
```
This flow does work, but just as easily, the generated project from an initial run can be opened in Vivado and the ILA can be added to the synthesized design as normal (Open Synthesized Design > Set Up Debug). This project should be located at `chipyard/fpga/generated-src/<LONG_NAME>/<CONFIG_NAME>.xpr`.

In order to properly use the branch monitor in its current state, `dontTouch` attributes needs to be added to the design to make sure it does not get eliminated from dead-code elimination during compilation/synthesis. Unfortunately, the Chisel `dontTouch` construct is not working entirely as [expected](https://stackoverflow.com/questions/49257904/doubts-about-chisel-donttouch-api-and-firrtl-optimiztion) and there are open [issues](https://github.com/chipsalliance/firrtl/issues/611) on the matter. The modified ROB code containing the branch monitor does have the construct added, but it does not impact the generated collateral found in `chipyard/fpga/generated-src/<LONG_NAME>/gen-collateral/Rob.sv`. The current compilation can be seen below for one of the branch monitor's signals `spec_br_inst`.

ROB implementation:

```
val spec_br_inst        = RegInit(VecInit(Seq.fill(maxBrCount)(0.U(32.W))))
...
dontTouch(spec_br_inst)

```

Generated `Rob.sv`:
```
reg  [31:0]       spec_br_inst_0;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_1;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_2;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_3;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_4;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_5;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_6;	// @[rob.scala:565:38]
reg  [31:0]       spec_br_inst_7;	// @[rob.scala:565:38]
```

In order to preserve the branch monitor, the generated SystemVerilog will have to be updated manually for all of the corresponding signals (not just `spec_br_inst`).

Updated `Rob.sv`:
```
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_0;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_1;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_2;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_3;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_4;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_5;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_6;	// @[rob.scala:565:38]
(* dont_touch = "true" *) reg  [31:0]       spec_br_inst_7;	// @[rob.scala:565:38]
```
With these updates, the signals can be searched for during ILA insertion. Then, after generating the updated bitstream in Vivado, the trigger for the ILA can be set to observe the `start_cnt` signal--which is set to one when the first trigger instruction is seen by the branch monitor (make sure that the trigger instruction in `rob.scala` matches the expected value for the targeted binary before generating a bitstream for the design). 
