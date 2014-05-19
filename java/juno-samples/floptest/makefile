.PHONY: clean run

default: run

run: simv
	./simv +vera_load=floptest.vro +filename=floptest.dat

simv: dut.v dut.test_top.v floptest.vro 
	$(VCS_HOME)/bin/vcs -vera dut.v dut.test_top.v floptest_shell.v

dut.test_top.v: dut.v
	$(VERA_HOME)/bin/vera -tem -t dut -c clk dut.v

floptest.vro: floptest.vr
	$(VERA_HOME)/bin/vera -cmp -vlog floptest.vr

clean:
	rm -rf csrc simv* vcs.key *.vro *tmp *_shell.v
