package sifive.fpgashells.shell.xilinx

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.SyncResetSynchronizerShiftReg
import sifive.fpgashells.clocks._
import sifive.fpgashells.shell._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.devices.xilinx.xilinxzcu106mig._

class SysClockZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: ClockInputDesignInput, val shellInput: ClockInputShellInput)
  extends LVDSClockInputXilinxPlacedOverlay(name, designInput, shellInput)
{
  val node = shell { ClockSourceNode(freqMHz = 300, jitterPS = 50) }

  shell { InModuleBody {
    shell.xdc.addPackagePin(io.p, "AH12")
    shell.xdc.addPackagePin(io.n, "AJ12")
    shell.xdc.addIOStandard(io.p, "DIFF_SSTL12")
    shell.xdc.addIOStandard(io.n, "DIFF_SSTL12")
  } }
}
class SysClockZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: ClockInputShellInput)(implicit val valName: ValName)
  extends ClockInputShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: ClockInputDesignInput) = new SysClockZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//PMOD JA used for SDIO
class SDIOZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: SPIDesignInput, val shellInput: SPIShellInput)
  extends SDIOXilinxPlacedOverlay(name, designInput, shellInput)
{
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
      shell.xdc.addIOB(io)
    } }
    packagePinsWithPackageIOs drop 1 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class SDIOZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: SPIShellInput)(implicit val valName: ValName)
  extends SPIShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: SPIDesignInput) = new SDIOZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class SPIFlashZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: SPIFlashDesignInput, val shellInput: SPIFlashShellInput)
  extends SPIFlashXilinxPlacedOverlay(name, designInput, shellInput)
{

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
class SPIFlashZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: SPIFlashShellInput)(implicit val valName: ValName)
  extends SPIFlashShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: SPIFlashDesignInput) = new SPIFlashZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class TracePMODZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: TracePMODDesignInput, val shellInput: TracePMODShellInput)
  extends TracePMODXilinxPlacedOverlay(name, designInput, shellInput, packagePins = Seq("U12", "V12", "V10", "V11", "U14", "V14", "T13", "U13"))
class TracePMODZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: TracePMODShellInput)(implicit val valName: ValName)
  extends TracePMODShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: TracePMODDesignInput) = new TracePMODZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class GPIOPMODZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: GPIOPMODDesignInput, val shellInput: GPIOPMODShellInput)
  extends GPIOPMODXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(("B23", IOPin(io.gpio_pmod_0)), //These are PMOD J55
                                        ("A23", IOPin(io.gpio_pmod_1)),
                                        ("F25", IOPin(io.gpio_pmod_2)),
                                        ("E20", IOPin(io.gpio_pmod_3)),
                                        ("K24", IOPin(io.gpio_pmod_4)),
                                        ("L23", IOPin(io.gpio_pmod_5)),
                                        ("L22", IOPin(io.gpio_pmod_6)),
                                        ("D7" , IOPin(io.gpio_pmod_7)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
    packagePinsWithPackageIOs drop 7 foreach { case (pin, io) => {
      shell.xdc.addPullup(io)
    } }
  } }
}
class GPIOPMODZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: GPIOPMODShellInput)(implicit val valName: ValName)
  extends GPIOPMODShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: GPIOPMODDesignInput) = new GPIOPMODZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class UARTZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: UARTDesignInput, val shellInput: UARTShellInput)
  extends UARTXilinxPlacedOverlay(name, designInput, shellInput, false)
{
  shell { InModuleBody {
    val packagePinsWithPackageIOs = Seq(
                                        // ("AP17", IOPin(io.ctsn.get)),
                                        // ("AM15", IOPin(io.rtsn.get)),
                                        ("AH17", IOPin(io.rxd)),
                                        ("AL17", IOPin(io.txd)))

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS12")
      shell.xdc.addIOB(io)
    } }
  } }
}
class UARTZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: UARTShellInput)(implicit val valName: ValName)
  extends UARTShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: UARTDesignInput) = new UARTZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

object LEDZCU106TSIPinConstraints{
  val pins = Seq("AL11", "AL13", "AK13", "AE15", "AM8", "AM9", "AM10", "AM11")
}
class LEDZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: LEDDesignInput, val shellInput: LEDShellInput)
  extends LEDXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(LEDZCU106TSIPinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class LEDZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: LEDShellInput)(implicit val valName: ValName)
  extends LEDShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: LEDDesignInput) = new LEDZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//SWs
object SwitchZCU106TSIPinConstraints{
  val pins = Seq("A17", "A16", "B16", "B15", "A15", "A14", "B14", "B13")
}
class SwitchZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: SwitchDesignInput, val shellInput: SwitchShellInput)
  extends SwitchXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(SwitchZCU106TSIPinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS18")
class SwitchZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: SwitchShellInput)(implicit val valName: ValName)
  extends SwitchShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: SwitchDesignInput) = new SwitchZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//Buttons
object ButtonZCU106TSIPinConstraints {
  val pins = Seq("AG13", "AC14", "AK12", "AP20", "AL10")
}
class ButtonZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: ButtonDesignInput, val shellInput: ButtonShellInput)
  extends ButtonXilinxPlacedOverlay(name, designInput, shellInput, packagePin = Some(ButtonZCU106TSIPinConstraints.pins(shellInput.number)), ioStandard = "LVCMOS12")
class ButtonZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: ButtonShellInput)(implicit val valName: ValName)
  extends ButtonShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: ButtonDesignInput) = new ButtonZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

class JTAGDebugBScanZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: JTAGDebugBScanDesignInput, val shellInput: JTAGDebugBScanShellInput)
 extends JTAGDebugBScanXilinxPlacedOverlay(name, designInput, shellInput)
class JTAGDebugBScanZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: JTAGDebugBScanShellInput)(implicit val valName: ValName)
  extends JTAGDebugBScanShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: JTAGDebugBScanDesignInput) = new JTAGDebugBScanZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

// PMOD J87 used for JTAG
class JTAGDebugZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: JTAGDebugDesignInput, val shellInput: JTAGDebugShellInput)
  extends JTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCK", IOPin(io.jtag_TCK), 10)
    shell.sdc.addGroup(clocks = Seq("JTCK"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.jtag_TCK))            //Untested
    val packagePinsWithPackageIOs = Seq(("AP11", IOPin(io.jtag_TCK)), //PMOD1_2
                                        ("AP10", IOPin(io.jtag_TMS)), //PMOD1_5
                                        ("AP9", IOPin(io.jtag_TDI)),  //PMOD1_4
                                        ("AN8", IOPin(io.jtag_TDO)),  //PMOD1_0
                                        ("AN9", IOPin(io.srst_n)))    //PMOD1_1

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
      shell.xdc.addPullup(io)
    } }
  } }
}
class JTAGDebugZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: JTAGDebugShellInput)(implicit val valName: ValName)
  extends JTAGDebugShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: JTAGDebugDesignInput) = new JTAGDebugZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//cjtag
class cJTAGDebugZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: cJTAGDebugDesignInput, val shellInput: cJTAGDebugShellInput)
  extends cJTAGDebugXilinxPlacedOverlay(name, designInput, shellInput)
{
  shell { InModuleBody {
    shell.sdc.addClock("JTCKC", IOPin(io.cjtag_TCKC), 10)
    shell.sdc.addGroup(clocks = Seq("JTCKC"))
    shell.xdc.clockDedicatedRouteFalse(IOPin(io.cjtag_TCKC))
    val packagePinsWithPackageIOs = Seq(("AP12", IOPin(io.cjtag_TCKC)),  //pin PMOD1_6
                                        ("AN12", IOPin(io.cjtag_TMSC)),  //pin PMOD1_7
                                        ("AN11", IOPin(io.srst_n)))      //PMOD1_3

    packagePinsWithPackageIOs foreach { case (pin, io) => {
      shell.xdc.addPackagePin(io, pin)
      shell.xdc.addIOStandard(io, "LVCMOS18")
    } }
      shell.xdc.addPullup(IOPin(io.cjtag_TCKC))
      shell.xdc.addPullup(IOPin(io.srst_n))
  } }
}
class cJTAGDebugZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: cJTAGDebugShellInput)(implicit val valName: ValName)
  extends cJTAGDebugShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: cJTAGDebugDesignInput) = new cJTAGDebugZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

case object ZCU106TSIDDRSize extends Field[BigInt](0x40000000L * 2) // 2 GB
class DDRZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: DDRDesignInput, val shellInput: DDRShellInput)
  extends DDRPlacedOverlay[XilinxZCU106MIGPads](name, designInput, shellInput)
{
  val size = p(ZCU106TSIDDRSize)

  val ddrClk1 = shell { ClockSinkNode(freqMHz = 300)}
  val ddrClk2 = shell { ClockSinkNode(freqMHz = 200)}
  val ddrGroup = shell { ClockGroup() }
  ddrClk1 := di.wrangler := ddrGroup := di.corePLL
  ddrClk2 := di.wrangler := ddrGroup
  
  val migParams = XilinxZCU106MIGParams(address = AddressSet.misaligned(di.baseAddress, size))
  val mig = LazyModule(new XilinxZCU106MIG(migParams))
  val ioNode = BundleBridgeSource(() => mig.module.io.cloneType)
  val topIONode = shell { ioNode.makeSink() }
  val ddrUI     = shell { ClockSourceNode(freqMHz = 300) }
  val areset    = shell { ClockSinkNode(Seq(ClockSinkParameters())) }
  areset := di.wrangler := ddrUI

  def overlayOutput = DDROverlayOutput(ddr = mig.node)
  def ioFactory = new XilinxZCU106MIGPads(size)

  InModuleBody { ioNode.bundle <> mig.module.io }

  shell { InModuleBody {
    require (shell.sys_clock.get.isDefined, "Use of DDRZCU106TSIPlacedOverlay depends on SysClockZCU106TSIPlacedOverlay")
    val (sys, _) = shell.sys_clock.get.get.overlayOutput.node.out(0)
    val (ui, _) = ddrUI.out(0)
    //val (dclk1, _) = ddrClk1.in(0)
    //val (dclk2, _) = ddrClk2.in(0)
    val (ar, _) = areset.in(0)
    val port = topIONode.bundle.port
    
    io <> port
    ui.clock := port.c0_ddr4_ui_clk
    ui.reset := /*!port.mmcm_locked ||*/ port.c0_ddr4_ui_clk_sync_rst
    port.c0_sys_clk_i := sys.clock.asUInt //dclk1.clock.asUInt
    //port.clk_ref_i := dclk2.clock.asUInt
    port.sys_rst := sys.reset
    port.c0_ddr4_aresetn := !ar.reset
    
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

  shell.sdc.addGroup(clocks = Seq("clk_pll_i"), pins = Seq(mig.island.module.blackbox.io.c0_ddr4_ui_clk))
}
class DDRZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: DDRShellInput)(implicit val valName: ValName)
  extends DDRShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: DDRDesignInput) = new DDRZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}

//Core to shell external resets
class CTSResetZCU106TSIPlacedOverlay(val shell: ZCU106TSIShellBasicOverlays, name: String, val designInput: CTSResetDesignInput, val shellInput: CTSResetShellInput)
  extends CTSResetPlacedOverlay(name, designInput, shellInput)
class CTSResetZCU106TSIShellPlacer(val shell: ZCU106TSIShellBasicOverlays, val shellInput: CTSResetShellInput)(implicit val valName: ValName)
  extends CTSResetShellPlacer[ZCU106TSIShellBasicOverlays] {
  def place(designInput: CTSResetDesignInput) = new CTSResetZCU106TSIPlacedOverlay(shell, valName.name, designInput, shellInput)
}


abstract class ZCU106TSIShellBasicOverlays()(implicit p: Parameters) extends Series7Shell {
  // Order matters; ddr depends on sys_clock
  val sys_clock = Overlay(ClockInputOverlayKey, new SysClockZCU106TSIShellPlacer(this, ClockInputShellInput()))
  val led       = Seq.tabulate(8)(i => Overlay(LEDOverlayKey, new LEDZCU106TSIShellPlacer(this, LEDShellInput(color = "red", number = i))(valName = ValName(s"led_$i"))))
  val switch    = Seq.tabulate(8)(i => Overlay(SwitchOverlayKey, new SwitchZCU106TSIShellPlacer(this, SwitchShellInput(number = i))(valName = ValName(s"switch_$i"))))
  val button    = Seq.tabulate(5)(i => Overlay(ButtonOverlayKey, new ButtonZCU106TSIShellPlacer(this, ButtonShellInput(number = i))(valName = ValName(s"button_$i"))))
  val ddr       = Overlay(DDROverlayKey, new DDRZCU106TSIShellPlacer(this, DDRShellInput()))
  val uart      = Overlay(UARTOverlayKey, new UARTZCU106TSIShellPlacer(this, UARTShellInput()))
  val sdio      = Overlay(SPIOverlayKey, new SDIOZCU106TSIShellPlacer(this, SPIShellInput()))
  val jtag      = Overlay(JTAGDebugOverlayKey, new JTAGDebugZCU106TSIShellPlacer(this, JTAGDebugShellInput()))
  val cjtag     = Overlay(cJTAGDebugOverlayKey, new cJTAGDebugZCU106TSIShellPlacer(this, cJTAGDebugShellInput()))
  val spi_flash = Overlay(SPIFlashOverlayKey, new SPIFlashZCU106TSIShellPlacer(this, SPIFlashShellInput()))
  val cts_reset = Overlay(CTSResetOverlayKey, new CTSResetZCU106TSIShellPlacer(this, CTSResetShellInput()))
  val jtagBScan = Overlay(JTAGDebugBScanOverlayKey, new JTAGDebugBScanZCU106TSIShellPlacer(this, JTAGDebugBScanShellInput()))
}

class ZCU106TSIShell()(implicit p: Parameters) extends ZCU106TSIShellBasicOverlays
{
  val resetPin = InModuleBody { Wire(Bool()) }
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))
  
  override lazy val module = new Impl
  class Impl extends LazyRawModuleImp(this) {

    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "G13")
    xdc.addIOStandard(reset, "LVCMOS18")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockZCU106TSIPlacedOverlay) => x.clock
    }

    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    resetPin := reset_ibuf.io.O

    pllReset := (reset_ibuf.io.O || powerOnReset) 
  }
}

class ZCU106TSIShellGPIOPMOD()(implicit p: Parameters) extends ZCU106TSIShellBasicOverlays
//This is the Shell used for coreip arty builds, with GPIOS and trace signals on the pmods
{
  // PLL reset causes
  val pllReset = InModuleBody { Wire(Bool()) }

  val gpio_pmod = Overlay(GPIOPMODOverlayKey, new GPIOPMODZCU106TSIShellPlacer(this, GPIOPMODShellInput()))
  val trace_pmod = Overlay(TracePMODOverlayKey, new TracePMODZCU106TSIShellPlacer(this, TracePMODShellInput()))

  val topDesign = LazyModule(p(DesignKey)(designParameters))

  // Place the sys_clock at the Shell if the user didn't ask for it
  p(ClockInputOverlayKey).foreach(_.place(ClockInputDesignInput()))

  override lazy val module = new LazyRawModuleImp(this) {
    val reset = IO(Input(Bool()))
    xdc.addPackagePin(reset, "G13")
    xdc.addIOStandard(reset, "LVCMOS18")

    val reset_ibuf = Module(new IBUF)
    reset_ibuf.io.I := reset

    val sysclk: Clock = sys_clock.get() match {
      case Some(x: SysClockZCU106TSIPlacedOverlay) => x.clock
    }

    val powerOnReset = PowerOnResetFPGAOnly(sysclk)
    sdc.addAsyncPath(Seq(powerOnReset))

    val ctsReset: Bool = cts_reset.get() match {
      case Some(x: CTSResetZCU106TSIPlacedOverlay) => x.designInput.rst
      case None => false.B
    }

    pllReset :=  (reset_ibuf.io.O || powerOnReset || ctsReset)
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
