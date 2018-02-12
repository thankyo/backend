package com.clemble.loveit.payment.model

import java.time.LocalDate

import com.clemble.loveit.thank.model.Project

case class PendingCharge (
                           project: Project,
                           transactions: List[PendingTransaction],
                           created: LocalDate
                        )
