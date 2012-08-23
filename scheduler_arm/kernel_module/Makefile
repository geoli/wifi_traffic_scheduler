# Makefile for the k_flow_control module (k_flow_control.c) - target architecture : ARM

obj-m += k_flow_control.o
# the parent dir of kernel source root dir
BASEDIR	= ~/nexusKernel
export BASEDIR
# the kernel source root dir
KDIR	= samsung/
export KDIR

PWD = `pwd`
export PWD

all:
	make ARCH=arm CROSS_COMPILE=$(CROSS_COMPILE) -C $(BASEDIR)/$(KDIR) M=$(PWD) modules

clean:
	make ARCH=arm CROSS_COMPILE=$(CROSS_COMPILE) -C $(BASEDIR)/$(KDIR) M=$(PWD) clean
