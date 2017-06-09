package com.clemble.loveit.payment.model

import com.clemble.loveit.common.SerializationSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EndOfMonthProcessingSerializationSpec extends SerializationSpec[EOMStatus]
