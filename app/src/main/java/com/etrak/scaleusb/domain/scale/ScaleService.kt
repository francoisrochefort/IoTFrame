package com.etrak.scaleusb.domain.scale

import com.etrak.scaleusb.api.mc.McService

class ScaleService : McService(ScaleEmulator::class.java)