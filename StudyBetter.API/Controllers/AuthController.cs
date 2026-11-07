
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using Google.Apis.Auth;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using StudyBetter.API.Data;
using StudyBetter.API.DTOs;
using StudyBetter.API.Models;
using Microsoft.AspNetCore.Authorization;

namespace StudyBetter.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly StudyBetterDbContext _context;
        private readonly PasswordHasher<User> _passwordHasher;
        private readonly IConfiguration _configuration;

        public AuthController(
            StudyBetterDbContext context,
            IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
            _passwordHasher = new PasswordHasher<User>();
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register(RegisterRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.FullName))
                return BadRequest(new { message = "Full name is required." });

            if (string.IsNullOrWhiteSpace(request.Email))
                return BadRequest(new { message = "Email is required." });

            if (string.IsNullOrWhiteSpace(request.Password))
                return BadRequest(new { message = "Password is required." });

            if (request.Password.Length < 6)
                return BadRequest(new { message = "Password must contain at least 6 characters." });

            var email = request.Email.Trim().ToLowerInvariant();

            var existingUser = await _context.Users
                .FirstOrDefaultAsync(user => user.Email == email);

            if (existingUser != null)
                return Conflict(new { message = "An account with this email already exists." });

            var user = new User
            {
                FullName = request.FullName.Trim(),
                Email = email
            };

            user.PasswordHash = _passwordHasher.HashPassword(user, request.Password);

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return Ok(new AuthResponse
            {
                UserID = user.UserID,
                FullName = user.FullName,
                Email = user.Email,
                Message = "Registration successful."
            });
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Email))
                return BadRequest(new { message = "Email is required." });

            if (string.IsNullOrWhiteSpace(request.Password))
                return BadRequest(new { message = "Password is required." });

            var email = request.Email.Trim().ToLowerInvariant();

            var user = await _context.Users
                .FirstOrDefaultAsync(user => user.Email == email);

            if (user == null)
                return Unauthorized(new { message = "Invalid email or password." });

            var passwordResult = _passwordHasher.VerifyHashedPassword(
                user, user.PasswordHash, request.Password);

            if (passwordResult == PasswordVerificationResult.Failed)
                return Unauthorized(new { message = "Invalid email or password." });

            return Ok(CreateAuthResponse(user, "Login successful."));
        }

        [HttpPost("google")]
        public async Task<IActionResult> GoogleLogin(StudyBetter.API.DTOs.GoogleLoginRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.IdToken))
                return BadRequest(new { message = "Google ID token is required." });

            var googleClientId = _configuration["Google:ClientId"];

            if (string.IsNullOrWhiteSpace(googleClientId))
                return StatusCode(500, new { message = "Google authentication is not configured." });

            GoogleJsonWebSignature.Payload payload;

            try
            {
                var validationSettings = new GoogleJsonWebSignature.ValidationSettings
                {
                    Audience = new[] { googleClientId }
                };

                payload = await GoogleJsonWebSignature.ValidateAsync(
                    request.IdToken,
                    validationSettings
                );
            }
            catch
            {
                return Unauthorized(new { message = "Invalid Google authentication token." });
            }

            if (string.IsNullOrWhiteSpace(payload.Email) || !payload.EmailVerified)
                return Unauthorized(new { message = "Your Google email address could not be verified." });

            var email = payload.Email.Trim().ToLowerInvariant();

            var user = await _context.Users
                .FirstOrDefaultAsync(existingUser => existingUser.Email == email);

            if (user == null)
            {
                user = new User
                {
                    FullName = string.IsNullOrWhiteSpace(payload.Name)
                        ? email.Split('@')[0]
                        : payload.Name,
                    Email = email
                };

                // Google users authenticate through Google, not this generated password.
                var randomPassword = Convert.ToBase64String(
                    RandomNumberGenerator.GetBytes(48)
                );

                user.PasswordHash = _passwordHasher.HashPassword(
                    user,
                    randomPassword
                );

                _context.Users.Add(user);
                await _context.SaveChangesAsync();
            }

            return Ok(CreateAuthResponse(user, "Google login successful."));
        }

        private AuthResponse CreateAuthResponse(User user, string message)
        {
            var claims = new[]
            {
                new Claim(JwtRegisteredClaimNames.Sub, user.UserID.ToString()),
                new Claim(JwtRegisteredClaimNames.Email, user.Email),
                new Claim("fullName", user.FullName)
            };

            var key = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(_configuration["Jwt:Key"]!)
            );

            var credentials = new SigningCredentials(
                key,
                SecurityAlgorithms.HmacSha256
            );

            var token = new JwtSecurityToken(
                issuer: _configuration["Jwt:Issuer"],
                audience: _configuration["Jwt:Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddHours(2),
                signingCredentials: credentials
            );

            return new AuthResponse
            {
                UserID = user.UserID,
                FullName = user.FullName,
                Email = user.Email,
                Token = new JwtSecurityTokenHandler().WriteToken(token),
                Message = message
            };
        }

        [Authorize]
        [HttpGet("me")]
        public async Task<IActionResult> GetCurrentUser()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value
                ?? User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;

            if (string.IsNullOrEmpty(userIdClaim))
                return Unauthorized(new { message = "User ID could not be found in token." });

            if (!int.TryParse(userIdClaim, out int userId))
                return Unauthorized(new { message = "Invalid user ID in token." });

            var user = await _context.Users.FindAsync(userId);

            if (user == null)
                return NotFound(new { message = "User not found." });

            return Ok(new
            {
                user.UserID,
                user.FullName,
                user.Email,
                user.Language,
                user.StudyGoal,
                user.Theme,
                user.NotificationsEnabled
            });
        }
    }
}
