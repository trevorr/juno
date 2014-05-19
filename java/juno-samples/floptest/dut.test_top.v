module dut_test_top;
  parameter simulation_cycle = 100 ;
  
  reg  SystemClock ;
  wire  clk ;
  wire  [31:0]  num_clks ;
  wire  [31:0]  flop_in ;
  wire  [31:0]  flop_out ;
  assign  clk = SystemClock ;

  vera_shell vshell(Systemclock);

`ifdef emu
/* DUT is in emulator, so not instantiated here */
`else
  dut dut(
    .clk ( clk ),
    .num_clks ( num_clks ),
    .flop_in ( flop_in ),
    .flop_out ( flop_out )
  );
`endif

  initial begin
    SystemClock = 0 ;
    forever begin
      #(simulation_cycle/2) 
        SystemClock = ~SystemClock ;
    end
  end
  
endmodule  
