package com.clemble.loveit.common.model

import java.time.LocalDateTime

import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.user.model.UserAware

case class ThankEvent(
                       user: UserID,
                       project: Project,
                       url: Resource,
                       created: LocalDateTime = LocalDateTime.now()
                     ) extends ResourceAware with CreatedAware with UserAware
