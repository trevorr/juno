module dut(clk, num_clks, flop_in, flop_out);
    input clk;
    input[31:0] flop_in;
    output[31:0] num_clks;
    output[31:0] flop_out;
    reg[31:0] num_clks;
    reg[31:0] flop_out;

    initial
    begin
        num_clks = 0;
    end

    always @(posedge clk)
        num_clks = num_clks + 1;

    always @(posedge clk)
        flop_out = flop_in;

endmodule
