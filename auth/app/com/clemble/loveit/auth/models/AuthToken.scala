package com.clemble.loveit.auth.models

import java.util.UUID

import com.clemble.loveit.common.model.UserID

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param id The unique token ID.
 * @param userID The unique ID of the user the token is associated with.
 */
case class AuthToken(
                      id: UUID,
                      userID: UserID
)
