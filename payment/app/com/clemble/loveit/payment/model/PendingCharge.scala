package com.clemble.loveit.payment.model

import java.time.LocalDate

import com.clemble.loveit.thank.model.SupportedProject

case class PendingCharge (
                          project: SupportedProject,
                          transactions: List[PendingTransaction],
                          created: LocalDate
                        )
