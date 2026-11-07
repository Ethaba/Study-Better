
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using StudyBetter.API.Data;
using StudyBetter.API.DTOs;
using StudyBetter.API.Models;

namespace StudyBetter.API.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]")]
    public class StudySessionsController : ControllerBase
    {
        private readonly StudyBetterDbContext _context;

        public StudySessionsController(StudyBetterDbContext context)
        {
            _context = context;
        }

        private int? GetCurrentUserId()
        {
            string? userId =
                User.FindFirst(ClaimTypes.NameIdentifier)?.Value ??
                User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;

            if (int.TryParse(userId, out int id))
                return id;

            return null;
        }

        // GET: api/StudySessions
        [HttpGet]
        public async Task<IActionResult> GetStudySessions()
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var sessions = await _context.StudySessions
                .Where(s => s.UserID == userId.Value)
                .OrderByDescending(s => s.StartedAt)
                .Select(s => new StudySessionResponse
                {
                    StudySessionID = s.StudySessionID,
                    StartedAt = s.StartedAt,
                    DurationSeconds = s.DurationSeconds,
                    Description = s.Description,
                    CreatedAt = s.CreatedAt
                })
                .ToListAsync();

            return Ok(sessions);
        }

        // GET: api/StudySessions/5
        [HttpGet("{id}")]
        public async Task<IActionResult> GetStudySession(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var session = await _context.StudySessions
                .Where(s => s.StudySessionID == id &&
                            s.UserID == userId.Value)
                .Select(s => new StudySessionResponse
                {
                    StudySessionID = s.StudySessionID,
                    StartedAt = s.StartedAt,
                    DurationSeconds = s.DurationSeconds,
                    Description = s.Description,
                    CreatedAt = s.CreatedAt
                })
                .FirstOrDefaultAsync();

            if (session == null)
                return NotFound(new { message = "Study session not found." });

            return Ok(session);
        }

        // POST: api/StudySessions
        [HttpPost]
        public async Task<IActionResult> CreateStudySession(
            [FromBody] StudySessionRequest request)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            if (request.DurationSeconds <= 0)
                return BadRequest(new { message = "Study duration must be greater than zero." });

            if (request.StartedAt == default)
                return BadRequest(new { message = "A valid start date is required." });

            var session = new StudySession
            {
                UserID = userId.Value,
                StartedAt = request.StartedAt,
                DurationSeconds = request.DurationSeconds,
                Description = request.Description?.Trim() ?? string.Empty,
                CreatedAt = DateTime.UtcNow
            };

            _context.StudySessions.Add(session);
            await _context.SaveChangesAsync();

            var response = new StudySessionResponse
            {
                StudySessionID = session.StudySessionID,
                StartedAt = session.StartedAt,
                DurationSeconds = session.DurationSeconds,
                Description = session.Description,
                CreatedAt = session.CreatedAt
            };

            return CreatedAtAction(
                nameof(GetStudySession),
                new { id = session.StudySessionID },
                response);
        }

        // DELETE: api/StudySessions/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteStudySession(int id)
        {
            var userId = GetCurrentUserId();

            if (userId == null)
                return Unauthorized();

            var session = await _context.StudySessions.FirstOrDefaultAsync(
                s => s.StudySessionID == id &&
                     s.UserID == userId.Value);

            if (session == null)
                return NotFound(new { message = "Study session not found." });

            _context.StudySessions.Remove(session);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Study session deleted successfully." });
        }
    }
}
