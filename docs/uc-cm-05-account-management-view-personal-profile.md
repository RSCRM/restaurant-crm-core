# UC-CM-05 - Account management: View personal profile

## Business analysis

- **Actor:** Authenticated user.
- **Goal:** View the profile of the currently authenticated account.
- **Preconditions:** The token contains a valid `userId`.
- **Postcondition:** The system returns the authenticated user's account and profile information.

## Main flow

1. The user opens their personal profile.
2. The system resolves `userId` from the authentication context.
3. The system loads the user account and its optional profile record.
4. The system returns the user's identity, contact information, status, roles, and creation time.

## Alternate flows

- If the user does not exist, the request is rejected with `USER_NOT_FOUND`.
- If no `user_profiles` record exists, account fields are still returned while `fullName` and `phone` are null.
- An expired or invalid token is rejected by authentication.
- A user cannot supply another `userId` to view a different profile.

## Acceptance criteria

1. The API returns only the profile associated with the token's `userId`.
2. The response contains `id`, `fullName`, `username`, `email`, `phone`, `status`, `roles`, and `createdAt`.
3. The request does not accept a user identifier from the client.
4. A missing optional profile does not hide the account information.
5. The operation does not modify account or profile data.

## API contract

`GET /api/v1/users/me`

Response: `ApiResponse<UserProfileResponse>`.

## Database design

The existing `users`, `user_profiles`, `roles`, and user-role relation are reused. `user_profiles.user_id` is unique and references `users.id`; `user_profiles.phone` is also unique.
