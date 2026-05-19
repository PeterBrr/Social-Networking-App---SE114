package com.example.lucincin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    // ================= GROUP: AUTH =================

    @POST("/api/login")
    Call<UserResponse> loginUser(@Body LoginRequest payload);

    @POST("/api/register")
    Call<UserResponse> registerUser(@Body RegisterRequest payload);

    @GET("/api/users/emails")
    Call<List<String>> getAllUserEmails();

    @GET("/api/users/{user_id}/friends")
    Call<FriendsResponse> getUserFriends(@Path("user_id") int userId);

    @GET("/api/users/{user_id}/profile")
    Call<UserResponse> getUserProfile(@Path("user_id") int userId);

    @PATCH("/api/users/{user_id}/profile")
    Call<UserResponse> updateUserProfile(@Path("user_id") int userId, @Body UpdateProfileRequest updateData);

    // ================= GROUP: POSTS =================

    @POST("/api/posts")
    Call<PostsResponse> createPost(@Body CreatePostRequest payload);

    @GET("/api/posts")
    Call<PostsResponse> getAllPosts();

    @GET("/api/posts/user/{user_id}")
    Call<PostsResponse> getUserPosts(@Path("user_id") int userId);

    @DELETE("/api/posts/{post_id}")
    Call<UserResponse> deletePost(@Path("post_id") int postId);
}
