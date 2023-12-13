package sifive.fpgashells.shell.xilinx

import chisel3._
import chisel3.experimental.{attach, Analog, IO}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.blocks.devices.chiplink._
import sifive.fpgashells.devices.xilinx.xilinxzcu106mig._
import sifive.fpgashells.devices.xilinx.xdma._
import sifive.fpgashells.ip.xilinx.xxv_ethernet._

class SysClockZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput) 
{
  //pg 48 of ZCU106 Board User Guide, changed 250 to default 300 to use USER_SI570 default freq
  //SI570 300MHz 
  val node = shell { ClockSourceNode(freqMHz = 300, jitterPS = 50)(ValName(name)) }
  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "AH12")
    shell.xdc.addPackagePin(io.n, "AJ12")
    shell.xdc.addIOStandard(io.p, "DIFF_SSTL12")
    shell.xdc.addIOStandard(io.n, "DIFF_SSTL12")
  //SI570 MGT Clock has two clock pairs (VCU118 used two clocks for the 2 DDR devices)
  // val node = shell { ClockSourceNode(freqMHz = 156.25, jitterPS = 50)(ValName(name)) }
  // shell { InModuleBody {
  //   shell.xdc.addPackagePin(io.p, "R10")
  //   shell.xdc.addPackagePin(io.n, "R9")
  //   shell.xdc.addIOStandard(io.p, "LVDS")
  //   shell.xdc.addIOStandard(io.n, "LVDS")
  } }
}
class SysClockZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[ZCU106ShellBasicOverlays]
{
    def place(designInput: ClockInputDesignInput) = new SysClockZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class RefClockZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput) 
{
  //pg 48 of ZCU106 Board User Guide
  val node = shell { ClockSourceNode(freqMHz = 125, jitterPS = 50)(ValName(name)) }
  
  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "H9")
    shell.xdc.addPackagePin(io.n, "G9")
    shell.xdc.addIOStandard(io.p, "LVDS")
    shell.xdc.addIOStandard(io.n, "LVDS")
  } }
}
class RefClockZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: ClockInputDesignInput) = new RefClockZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SDIOZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
  //Comparing the PMOD GPIO Headers between the two boards (VCU118 = pg 91 and ZCU106 = pg 83)
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("E20", IOPin(io.spi_clk)),      //PMOD0_3
                                        ("A23", IOPin(io.spi_cs)),       //PMOD0_1
                                        ("F25", IOPin(io.spi_dat(0))),   //PMOD0_2
                                        ("K24", IOPin(io.spi_dat(1))),   //PMOD0_4
                                        ("L23", IOPin(io.spi_dat(2))),   //PMOD0_5
                                        ("B23", IOPin(io.spi_dat(3))))   //PMOD0_0

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    } }
  } }
}
class SDIOZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: SPIDesignInput) = new SDIOZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SPIFlashZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{
  //Quad-SPI Component connected to FPGA: VCU118 pg. 39; ZCU106 pg. 39 (TODO: LWR/UPR?)
  shell { InModuleBody {
    /*Commented out in the VCU118 Design
    val packagePinsWithPackageIOs = Seq(("A24", IOPin(io.qspi_sck)),
      ("D25", IOPin(io.qspi_cs)), 
      ("A25", IOPin(io.qspi_dq(0))),
      ("C24", IOPin(io.qspi_dq(1))),
      ("B24", IOPin(io.qspi_dq(2))),
      ("E25", IOPin(io.qspi_dq(3))))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
    */
  } }
}
class SPIFlashZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, true)
{
  //Comparing the UART between the two boards (VCU118 = pg 81-82 and ZCU106 = pg 67-68), UART for ZCU106 doesn't seem t have cts/rts
  //TODO: UART2 which is a PL-Side USB UART has RTS pins, so I am using those for now and confirming later.
  shell { InModuleBody {  
    val packagePinsWithPackageIOs = Seq(("AP17", IOPin(io.ctsn.get)),
                                        ("AM15", IOPin(io.rtsn.get)),
                                        ("AH17", IOPin(io.rxd)),
                                        ("AL17", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS12") //TODO: ZCU106 maps these to bank 64 which is 1.2V, so changed from LVCMOS18 to LVCMOS12
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

//VCU118 QSFP Jitter Attenuated Clock (Si5328B) pg. 65-66; ZCU106 SFP/SFP+ (Si5328B) pg. 80-81
class QSFP1ZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: EthernetDesignInput, val shellInput: EthernetShellInput)
  extends EthernetUltraScalePlacedOverlay(name, designInput, shellInput, XXVEthernetParams(name = name, speed   = 10, dclkMHz = 125))
{
  val dclkSource = shell { BundleBridgeSource(() => Clock()) }
  val dclkSink = dclkSource.makeSink()
  InModuleBody {
    dclk := dclkSink.bundle
  }
  shell { InModuleBody {
    dclkSource.bundle := shell.ref_clock.get.get.overlayOutput.node.out(0)._1.clock
    shell.xdc.addPackagePin(io.tx_p, "Y4")
    shell.xdc.addPackagePin(io.tx_n, "Y3")
    shell.xdc.addPackagePin(io.rx_p, "AA2")
    shell.xdc.addPackagePin(io.rx_n, "AA1")
    shell.xdc.addPackagePin(io.refclk_p, "H11")
    shell.xdc.addPackagePin(io.refclk_n, "G11")
  } }
}
class QSFP1ZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: EthernetShellInput)(implicit val valName: ValName)
  extends EthernetShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: EthernetDesignInput) = new QSFP1ZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

//VCU118 QSFP Jitter Attenuated Clock (Si5328B) pg. 65-66; ZCU106 SFP/SFP+ (Si5328B) pg. 80-81, pins are udpated but removing for now
class QSFP2ZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: EthernetDesignInput, val shellInput: EthernetShellInput)
  extends EthernetUltraScalePlacedOverlay(name, designInput, shellInput, XXVEthernetParams(name = name, speed   = 10, dclkMHz = 125))
{
  val dclkSource = shell { BundleBridgeSource(() => Clock()) }
  val dclkSink = dclkSource.makeSink()
  InModuleBody {
    dclk := dclkSink.bundle
  }
  shell { InModuleBody {
    dclkSource.bundle := shell.ref_clock.get.get.overlayOutput.node.out(0)._1.clock
    shell.xdc.addPackagePin(io.tx_p, "W6")
    shell.xdc.addPackagePin(io.tx_n, "W5")
    shell.xdc.addPackagePin(io.rx_p, "W2")
    shell.xdc.addPackagePin(io.rx_n, "W1")
    shell.xdc.addPackagePin(io.refclk_p, "R10") //copied VCU118 using MGT_SI570_CLOCK2_C_[P,N]
    shell.xdc.addPackagePin(io.refclk_n, "R9")
  } }
}
class QSFP2ZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: EthernetShellInput)(implicit val valName: ValName)
  extends EthernetShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: EthernetDesignInput) = new QSFP2ZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object LEDZCU106PinConstraints {
  //GPIO_LED Pins VCU118: pg 90 ZCU106: pg 88
  val pins = Seq("AL11", "AL13", "AK13", "AE15", "AM8", "AM9", "AM10", "AM11")
}
class LEDZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(LEDZCU106PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class LEDZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object ButtonZCU106PinConstraints {
  //GPIO_SW_N, GPIO_SW_E,GPIO_SW_W, GPIO_SW_S, GPIO_SW_C (VCU118 bank 64 LVCMOS18 pg 90; ZCU106 LVCMOS12 pg 88)
  val pins = Seq("AG13", "AC14", "AK12", "AP20", "AL10")
}
class ButtonZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(ButtonZCU106PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class ButtonZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

object SwitchZCU106PinConstraints {
  //VCU118 LVCMOS12 4-Pole DOP SW12 pg 90; ZCU106 GPIO DIP SW 0-7 pg 88-89 LVCMOS18
  val pins = Seq("A17", "A16", "B16", "B15", "A15", "A14", "B14", "B13")
}
class SwitchZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(SwitchZCU106PinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS18")
class SwitchZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class ChipLinkZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: ChipLinkDesignInput, val shellInput: ChipLinkShellInput)
  extends ChipLinkXilinxPlacedOverlay(name, designInput, shellInput, rxPhase= -120, txPhase= -90, rxMargin=0.6, txMargin=0.5)
{
  val ereset_n = shell { InModuleBody {
    val ereset_n = IO(Analog(1.W))
    ereset_n.suggestName("ereset_n")
    val pin = IOPin(ereset_n, 0)
    shell.xdc.addPackagePin(pin, "E14") //VCU118 pg 98 FMC_HPC1_CLK0_M2C_N; ZCU106 pg 108 FMC_HPC0_CLK0_M2C_N
    shell.xdc.addIOStandard(pin, "LVCMOS18")
    shell.xdc.addTermination(pin, "NONE")
    shell.xdc.addPullup(pin)

    val iobuf = Module(new IOBUF)
    iobuf.suggestName("chiplink_ereset_iobuf")
    attach(ereset_n, iobuf.io.IO)
    iobuf.io.T := true.B // !oe
    iobuf.io.I := false.B

    iobuf.io.O
  } }

  //VCU118 seems to use HPC1 -> 68 single-ended or 34 differential user-definedd pairs (pg 96)
  //ZCU106 HPC1 has half the capability, but HPCO is defined the same (pg 104)
  shell { InModuleBody {
    val dir1 = Seq("E15", "D17", "C17", /* clk = FMC_HPC1_CLK0_M2C_P, rst = FMC_HPC1_LA16_N, send = FMC_HPC1_LA16_P*/
                  //FMC_HPC1_LA[00:15]..._P, FMC_HPC1_LA[00:15]..._N, 
                   "F17",  "F16",
                   "H18",  "H17",  
                   "L20",  "K20", 
                   "K19",  "K18", 
                   "L17",  "L16", 
                   "K17",  "J17", 
                   "H19",  "G19", 
                   "J16", "J15",
                   "E18", "E17", 
                   "H16", "G16", 
                   "L15", "K15", 
                   "A13", "A12",
                   "G18", "F18", 
                   "G15", "F15",  
                   "C13", "C12",  
                   "D16", "C16")
    val dir2 = Seq("G10", "C9", "C8", /* clk = FMC_HPC1_CLK1_M2C_P, rst = FMC_HPC1_LA33_N, send = FMC_HPC1_LA33_P*/
                   //FMC_HPC1_LA[17:32]_CC_P, FMC_HPC1_LA[17:32]_CC_N
                   "F11", "E10",
                   "D11", "D10", 
                   "D12", "C11", 
                   "F12", "D16",
                   "B10", "A10", 
                   "H13", "H12", 
                   "B11", "A11", 
                   "B6",  "A6",
                   "C7",  "C6", 
                   "B9",  "B8", 
                   "A8",  "A7", 
                   "M13", "L13",
                   "K10", "J10", 
                   "E9",  "D9", 
                   "F7",  "E7", 
                   "F8",  "E8")
    (IOPin.of(io.b2c) zip dir1) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
    (IOPin.of(io.c2b) zip dir2) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }
}
class ChipLinkZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: ChipLinkShellInput)(implicit val valName: ValName)
  extends ChipLinkShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: ChipLinkDesignInput) = new ChipLinkZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

// JTAG is untested (VCU118 pg 91-92; ZCU106 pg 83-84)
class JTAGDebugZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val pin_locations = Map(
      //PMOD Rt-Angle Female (VCU118 J52; ZCU106 J55) PMOD0_2, PMOD0_5, PMOD0_4, PMOD0_0, PMOD0_1
      "PMOD_J55" -> Seq("F25",      "L23",      "K24",      "B23",      "A23"),
      //PMOD Male Vertical   (VCU118 J53; ZCU106 J87) PMOD1_2, PMOD1_5, PMOD1_4, PMOD1_0, PMOD1_1
      "PMOD_J53" -> Seq( "AP11",       "AP10",       "AP9",       "AN8",       "AN9"),
      //VCU118 J2 FMC HPC 1 pg 99:  FMC_HPC1_LA30_N, FMC_HPC1_LA29_P, FMC_HPC1_LA29_N, FMC_HPC1_LA31_N, FMC_HPC1_LA30_P
      //ZCU106 J5 FMC HPC 0 pg 108: FMC_HPC0_LA30_N, FMC_HPC0_LA29_P, FMC_HPC0_LA29_N, FMC_HPC0_LA31_N, FMC_HPC0_LA30_P
      "FMC_J5"   -> Seq("D9",      "K10",      "J10",      "E7",      "E9"))
    val pins      = Seq(io.jtag_TCK, io.jtag_TMS, io.jtag_TDI, io.jtag_TDO, io.srst_n)

    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))

    val pin_voltage:String = "LVCMOS18"; //ZCU106 uses one I/O Standard VCU118 uses 2 if(shellInput.location.get == "PMOD_J53") "LVCMOS12" else "LVCMOS18"

    (pin_locations(shellInput.location.get) zip pins) foreach { case (pin_location, ioport) =>
      val io = IOPin(ioport)
      shell.xdc.addPackagePin(io, pin_location)
      shell.xdc.addIOStandard(io, pin_voltage)
      shell.xdc.addPullup(io)
      shell.xdc.addIOB(io)
    }
  } }
}
class JTAGDebugZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class cJTAGDebugZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: cJTAGDebugDesignInput, val shellInput: cJTAGDebugShellInput)
  extends cJTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCKC", IOPin(io.cjtag_TCKC), 10)
    shell.sdc.addGroup(clocks = Seq("JTCKC"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.cjtag_TCKC))
    val packagePinsWithPackageIOs = Seq(("F12", IOPin(io.cjtag_TCKC)), //VCU118 J2 FMC HPC 1 pg 99: FMC_HPC1_LA20_P; ZCU106 pg 108 FMC_HPC0_LA20_P
                                        ("B6",  IOPin(io.cjtag_TMSC)), //VCU118 J2 FMC HPC 1 pg 99: FMC_HPC1_LA24_P; ZCU106 pg 108 FMC_HPC0_LA24_P
                                        ("E12", IOPin(io.srst_n)))     //VCU118 J2 FMC HPC 1 pg 99: FMC_HPC1_LA20_N; ZCU106 pg 108 FMC_HPC0_LA20_N

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
      shell.xdc.addPullup(IOPin(io.cjtag_TCKC))
      shell.xdc.addPullup(IOPin(io.srst_n))
  } }
}
class cJTAGDebugZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: cJTAGDebugShellInput)(implicit val valName: ValName)
  extends cJTAGDebugShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: cJTAGDebugDesignInput) = new cJTAGDebugZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugBScanZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: JTAGDebugBScanDesignInput, val shellInput: JTAGDebugBScanShellInput)
  extends JTAGDebugBScanXilinxPlacedOverlay(name, designInput, shellInput)
class JTAGDebugBScanZCU106ShellPlacer(val shell: ZCU106ShellBasicOverlays, val shellInput: JTAGDebugBScanShellInput)(implicit val valName: ValName)
  extends JTAGDebugBScanShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: JTAGDebugBScanDesignInput) = new JTAGDebugBScanZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object ZCU106DDRSize extends Field[BigInt](0x40000000L * 2) // 2GB
class DDRZCU106PlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxZCU106MIGPads](name, designInput, shellInput)
{
  val size = p(ZCU106DDRSize)

  val migParams = XilinxZCU106MIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig = LazyModule(new XilinxZCU106MIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 300) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := designInput.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxZCU106MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRZCU106Overlay depends on SysClockZCU106Overlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    io <> port
    ui.clock := port.c0_ddr4_ui_clk
    ui.reset := /*!port.mmcm_locked ||*/ port.c0_ddr4_ui_clk_sync_rst
    port.c0_sys_clk_i := sys.clock.asUInt
    port.sys_rst := sys.reset // pllReset
    port.c0_ddr4_aresetn := !ar.reset
    //ZCU106 pg 34-37 VCU118 pg 23-32
    val allddrpins = Seq(  
      //DDR4_C1A[0-13]
      "AK9", "AG11", "AJ10", "AL8", "AK10", "AH8", "AJ9",
      "AG8", "AH9", "AG10", "AH13", "AG9", "AM13", "AF8", 
      //DDR4_C1_A14_WE_B, DDR4_C1_A15_CAS_B, DDR4_C1_A16_RAS_B
      "AC12", "AE12", "AF11",
      //DDR4_C1_BG0, DDR4_C1_BA0, DDR4_C1_BA1, DDR4_C1_RESET_B, DDR4_C1_ACT_B, 
      "AE14", "AK8", "AL12", "AF12", "AD14", 
      //DDR4_C1_CK_C, DDR4_C1_CK_T, DDR4_C1_CKE, DDR4_C1_CS_B, DDR4_C1_ODT
      "AJ11", "AH11", "AB13", "AD12", "AF10",
      //DDR4_C1_DQ[0-63]
      "AF16", "AF18", "AG15", "AF17",  "AF15", "AG18", "AG14",  "AE17",  "AA14", "AC16",
      "AB15", "AD16", "AB16", "AC17", "AB14", "AD17", "AJ16", "AJ17", "AL15", "AK17",
      "AJ15", "AK18", "AL16", "AL18", "AP13", "AP16", "AP15", "AN16", "AN13", "AM18",
      "AN17", "AN18", "AB19", "AD19", "AC18", "AC19", "AA20", "AE20", "AA19", "AD20",
      "AF22", "AH21", "AG19", "AG21", "AE24", "AG20", "AE23", "AF21", "AL22", "AJ22",
      "AL23", "AJ21", "AK20", "AJ19", "AK19", "AJ20", "AP22", "AN22", "AP21", "AP23",
      "AM19", "AM23", "AN19", "AN23", 
      //DDR4_C1_DQS[0-7]_C
      "AJ14", "AA15", "AK14", "AN14", "AB18", "AG23", "AK23", "AN21",
      //DDR4_C1_DQS[0-7]_T 
      "AH14", "AA16", "AK15", "AM14", "AA18", "AF23", "AK22", "AM21",
      //DDR4_C1_DM[0-7]
      "AH18", "AD15", "AM16", "AP18", "AE18", "AH22", "AL20", "AP19")

    (IOPin.of(io) zip allddrpins) foreach { case (io, pin) => shell.xdc.addPackagePin(io, pin) }
  } }

  shell.sdc.addGroup(pins = Seq(mig.island.module.blackbox.io.c0_ddr4_ui_clk))
}
class DDRZCU106ShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRZCU106PlacedOverlay(shell, valName.name, designInput, shellInput)
}

//TODO: VCU118 Has FMC+ (FMCP) support whereas the ZCU106 does not (going to just use HPC0 J5 for the ZCU106)
class PCIeZCU106FMCPlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: PCIeDesignInput, val shellInput: PCIeShellInput)
  extends PCIeUltraScalePlacedOverlay(name, designInput, shellInput, XDMAParams(
    name     = "fmc_xdma",
    location = "X0Y3", //TODO: Update for ZCU106?
    bars     = designInput.bars,
    control  = designInput.ecam,
    bases    = designInput.bases,
    lanes    = 4))
{
  shell { InModuleBody {
    // Work-around incorrectly pre-assigned pins (Note was for the VCU118, still applicable for the ZCU106?)
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }

    // We need some way to connect both of these to reach x8
    val ref226 = Seq("V8",  "V7") /* [pn] VCU118: GBT0 Bank 126 MGTREFCLK0[PN]_126/FMCP_HSPC_GBT0_1_; ZCU106: MGTREFCLK0/FMC_HPC0_GBTCLK0_M2C_C_*/
    val ref227 = Seq("T8",  "T7") /* [pn] VCU118: GBT0 Bank 121 MGTREFCLK0[PN]_121/FMCP_HSPC_GBT0_0_; ZCU106: MGTREFCLK0/FMC_HPC0_GBTCLK1_M2C_C_*/
    val ref = ref227

    //TODO: Finish the rx and tx mappings
    // VCU118 (pg 52-53): Bank 126 (DP5, DP6, DP4, DP7), Bank 121 (DP3, DP2, DP1, DP0)
    // ZCU106 (pg 91-92): Bank 227 (DP5, DP6, DP4, DP7), Bank 226 (DP3, DP2, DP1, DP0)
    val rxp = Seq("U45", "R45", "W45", "N45", "AJ45", "AL45", "AN45", "AR45") /* MGTYRXP1_126[0-7] */ //Order is 1, 6, 0, 3, 
    val rxn = Seq("U46", "R46", "W46", "N46", "AJ46", "AL46", "AN46", "AR46") /* [0-7] */
    val txp = Seq("P42", "M42", "T42", "K42", "AL40", "AM42", "AP42", "AT42") /* [0-7] */
    val txn = Seq("P43", "M43", "T43", "K43", "AL41", "AM43", "AP43", "AT43") /* [0-7] */

    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    bind(IOPin.of(io.refclk), ref)
    // We do these individually so that zip falls off the end of the lanes:
    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)
  } }
}
class PCIeZCU106FMCShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: PCIeShellInput)(implicit val valName: ValName)
  extends PCIeShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: PCIeDesignInput) = new PCIeZCU106FMCPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//VCU118 Supports GTY 8-lane edge connector (pg 52 & 60-64); the ZCU106 supports a GTH 4-lane edge connector P3 (pg 100 & 95)
class PCIeZCU106EdgePlacedOverlay(val shell: ZCU106ShellBasicOverlays, name: String, val designInput: PCIeDesignInput, val shellInput: PCIeShellInput)
  extends PCIeUltraScalePlacedOverlay(name, designInput, shellInput, XDMAParams(
    name     = "edge_xdma",
    location = "X1Y2", //TODO: Update this for the ZCU106?
    bars     = designInput.bars,
    control  = designInput.ecam,
    bases    = designInput.bases,
    lanes    = 4))
{
  shell { InModuleBody {
    // Work-around incorrectly pre-assigned pins (note was for the VCU118, does this still apply for the ZCU106?)
    IOPin.of(io).foreach { shell.xdc.addPackagePin(_, "") }
    /**VCU118
      PCIe Edge connector U2
       Lanes 00-03 Bank 227
       Lanes 04-07 Bank 226
       Lanes 08-11 Bank 225
       Lanes 12-15 Bank 224
    **/
    
    val ref224 = Seq("AB8", "AB7")  /* [pn]  VCU118 (pg 64) Bank 227 PCIE_CLK2_; ZCU106 (pg 96) PCI_CLK_*/
    val ref = ref224

    // PCIe Edge connector VCU 118 U2 : Bank 227, 226 (pg 72); ZCU106 Bank 224
    val rxp = Seq("AE2", "AF4", "AG2", "AJ2") // PCIE_RX_[0-7]_P; [0-3] for the ZCU106
    val rxn = Seq("AE1", "AF3", "AG1", "AJ1") // PCIE_RX_[0-7]_N; [0-3] for the ZCU106
    val txp = Seq("AD4", "AE6", "AG6", "AH4") // PCIE_TX_[0-7]_P; [0-3] for the ZCU106
    val txn = Seq("AD3", "AE5", "AG5", "AH3") // PCIE_TX_[0-7]_N; [0-3] for the ZCU106

    def bind(io: Seq[IOPin], pad: Seq[String]) {
      (io zip pad) foreach { case (io, pad) => shell.xdc.addPackagePin(io, pad) }
    }

    bind(IOPin.of(io.refclk), ref)
    // We do these individually so that zip falls off the end of the lanes:
    bind(IOPin.of(io.lanes.pci_exp_txp), txp)
    bind(IOPin.of(io.lanes.pci_exp_txn), txn)
    bind(IOPin.of(io.lanes.pci_exp_rxp), rxp)
    bind(IOPin.of(io.lanes.pci_exp_rxn), rxn)
  } }
}
class PCIeZCU106EdgeShellPlacer(shell: ZCU106ShellBasicOverlays, val shellInput: PCIeShellInput)(implicit val valName: ValName)
  extends PCIeShellPlacer[ZCU106ShellBasicOverlays] {
  def place(designInput: PCIeDesignInput) = new PCIeZCU106EdgePlacedOverlay(shell, valName.name, designInput, shellInput)
}

abstract class ZCU106ShellBasicOverlays()(implicit p: Parameters) extends UltraScaleShell{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockZCU106ShellPlacer(this, ClockInputShellInput()))
  val ref_clock = Overlay(ClockInputOverlayKey, new RefClockZCU106ShellPlacer(this, ClockInputShellInput()))
  val led       = Seq.tabulate(8)(i => Overlay(LEDOverlayKey, new LEDZCU106ShellPlacer(this, LEDShellInput(color = "red", number = i))(valName = ValName(s"led_$i"))))
  val switch    = Seq.tabulate(8)(i => Overlay(SwitchOverlayKey, new SwitchZCU106ShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  val button    = Seq.tabulate(5)(i => Overlay(ButtonOverlayKey, new ButtonZCU106ShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val ddr       = Overlay(DDROverlayKey, new DDRZCU106ShellPlacer(this, DDRShellInput()))
  val qsfp1     = Overlay(EthernetOverlayKey, new QSFP1ZCU106ShellPlacer(this, EthernetShellInput()))
  val qsfp2     = Overlay(EthernetOverlayKey, new QSFP2ZCU106ShellPlacer(this, EthernetShellInput()))
  val chiplink  = Overlay(ChipLinkOverlayKey, new ChipLinkZCU106ShellPlacer(this, ChipLinkShellInput()))
  //val spi_flash = Overlay(SPIFlashOverlayKey, new SPIFlashZCU106ShellPlacer(this, SPIFlashShellInput()))
  //SPI Flash not functional
}

case object ZCU106ShellPMOD extends Field[String]("JTAG")
case object ZCU106ShellPMOD2 extends Field[String]("JTAG")

class WithZCU106ShellPMOD(device: String) extends Config((site, here, up) => {
  case ZCU106ShellPMOD => device
})

// Due to the level shifter is from 1.2V to 3.3V, the frequency of JTAG should be slow down to 1Mhz
class WithZCU106ShellPMOD2(device: String) extends Config((site, here, up) => {
  case ZCU106ShellPMOD2 => device
})

class WithZCU106ShellPMODJTAG extends WithZCU106ShellPMOD("JTAG")
class WithZCU106ShellPMODSDIO extends WithZCU106ShellPMOD("SDIO")

// Reassign JTAG pinouts location to PMOD J53 (VCU118) or J87 (ZCU106)
class WithZCU106ShellPMOD2JTAG extends WithZCU106ShellPMOD2("PMODJ87_JTAG")

class ZCU106Shell()(implicit p: Parameters) extends ZCU106ShellBasicOverlays
{
  val pmod_is_sdio  = p(ZCU106ShellPMOD) == "SDIO"
  val pmod_j87_is_jtag = p(ZCU106ShellPMOD2) == "PMODJ87_JTAG"
  val jtag_location = Some(if (pmod_is_sdio) (if (pmod_j87_is_jtag) "PMODJ87_JTAG" else "FMC_J5") else "PMOD_J55")

  // Order matters; ddr depends on sys_clock
  val uart      = Overlay(UARTOverlayKey, new UARTZCU106ShellPlacer(this, UARTShellInput()))
  val sdio      = if (pmod_is_sdio) Some(Overlay(SPIOverlayKey, new SDIOZCU106ShellPlacer(this, SPIShellInput()))) else None
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugZCU106ShellPlacer(this, JTAGDebugShellInput(location = jtag_location)))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugZCU106ShellPlacer(this, cJTAGDebugShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanZCU106ShellPlacer(this, JTAGDebugBScanShellInput()))
  val fmc       = Overlay(PCIeOverlayKey, new PCIeZCU106FMCShellPlacer(this, PCIeShellInput()))
  val edge      = Overlay(PCIeOverlayKey, new PCIeZCU106EdgeShellPlacer(this, PCIeShellInput()))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  designParameters(ClockInputOverlayKey).foreach { unused =>
    val source = unused.place(ClockInputDesignInput()).overlayOutput.node
    val sink = ClockSinkNode(Seq(ClockSinkParameters()))
    sink := source
  }

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))

    //zcu106 pg 89, vcu118 pg 90
    xdc.addPackagePin(reset, "G13")
    xdc.addIOStandard(reset, "LVCMOS12")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockZCU106PlacedOverlay) => x.clock
    }

    val powerOnReset: Bool = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    val ereset: Bool = chiplink.get() match {
      case Some(x: ChipLinkZCU106PlacedOverlay) => !x.ereset_n
      case _ => false.B
    }

    pllReset := (reset_ibuf.io.O || powerOnReset || ereset)
  }
}

/*
   Copyright 2016 SiFive, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
