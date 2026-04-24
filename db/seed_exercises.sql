CREATE TABLE IF NOT EXISTS exercises (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    muscle_group VARCHAR(120),
    equipment VARCHAR(120),
    difficulty VARCHAR(80)
);

INSERT INTO exercises (id, name, muscle_group, equipment, difficulty)
SELECT (
        substr(md5(seed.name), 1, 8) || '-' ||
        substr(md5(seed.name), 9, 4) || '-' ||
        substr(md5(seed.name), 13, 4) || '-' ||
        substr(md5(seed.name), 17, 4) || '-' ||
        substr(md5(seed.name), 21, 12)
    )::uuid,
    seed.name,
    seed.muscle_group,
    seed.equipment,
    seed.difficulty
FROM (
    VALUES
        ('Bench Press', 'Chest', 'Barbell', 'Intermediate'),
        ('Incline Bench Press', 'Chest', 'Barbell', 'Intermediate'),
        ('Decline Bench Press', 'Chest', 'Barbell', 'Intermediate'),
        ('Dumbbell Press', 'Chest', 'Dumbbell', 'Beginner'),
        ('Chest Fly', 'Chest', 'Dumbbell', 'Beginner'),
        ('Pushups', 'Chest', 'Bodyweight', 'Beginner'),
        ('Pull-ups', 'Back', 'Bodyweight', 'Intermediate'),
        ('Lat Pulldown', 'Back', 'Cable', 'Beginner'),
        ('Barbell Row', 'Back', 'Barbell', 'Intermediate'),
        ('Seated Cable Row', 'Back', 'Cable', 'Beginner'),
        ('Deadlift', 'Back', 'Barbell', 'Advanced'),
        ('Squat', 'Legs', 'Barbell', 'Intermediate'),
        ('Leg Press', 'Legs', 'Machine', 'Beginner'),
        ('Lunges', 'Legs', 'Dumbbell', 'Beginner'),
        ('Leg Curl', 'Legs', 'Machine', 'Beginner'),
        ('Leg Extension', 'Legs', 'Machine', 'Beginner'),
        ('Calf Raises', 'Legs', 'Machine', 'Beginner'),
        ('Shoulder Press', 'Shoulders', 'Dumbbell', 'Intermediate'),
        ('Lateral Raise', 'Shoulders', 'Dumbbell', 'Beginner'),
        ('Front Raise', 'Shoulders', 'Dumbbell', 'Beginner'),
        ('Rear Delt Fly', 'Shoulders', 'Dumbbell', 'Beginner'),
        ('Bicep Curl', 'Arms', 'Dumbbell', 'Beginner'),
        ('Hammer Curl', 'Arms', 'Dumbbell', 'Beginner'),
        ('Tricep Pushdown', 'Arms', 'Cable', 'Beginner'),
        ('Skull Crushers', 'Arms', 'EZ Bar', 'Intermediate'),
        ('Dips', 'Arms', 'Bodyweight', 'Intermediate'),
        ('Plank', 'Core', 'Bodyweight', 'Beginner'),
        ('Crunches', 'Core', 'Bodyweight', 'Beginner'),
        ('Hanging Leg Raise', 'Core', 'Bodyweight', 'Intermediate'),
        ('Russian Twist', 'Core', 'Bodyweight', 'Beginner')
) AS seed(name, muscle_group, equipment, difficulty)
WHERE NOT EXISTS (
    SELECT 1 FROM exercises existing WHERE lower(existing.name) = lower(seed.name)
);
