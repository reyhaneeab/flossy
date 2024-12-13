<template>
  <div class="about-us-container">
    <div class="hero-section">
      <h1>About Us</h1>
      <p>Discover who we are, our mission, and what we stand for.</p>
    </div>

    <div class="content-section">
      <div class="mission">
        <h2>Our Mission</h2>
        <p>
          Our mission is to create innovative solutions that bring value to our users, empowering them to achieve their goals with ease and confidence.
        </p>
      </div>

      <div class="values">
        <h2>Our Values</h2>
        <ul>
          <li>Integrity: We uphold the highest standards in all our work.</li>
          <li>Innovation: Constantly evolving to meet the changing needs of our users.</li>
          <li>Collaboration: Building together for a better future.</li>
        </ul>
      </div>

      <div class="team">
        <h2>Meet the Team</h2>
        <p>
          We are a group of passionate individuals who work together to turn ideas into reality. Each team member brings unique skills and perspectives to the table.
        </p>
      </div>
    </div>
  </div>
</template>

<style>
.about-us-container {
  font-family: Arial, sans-serif;
  color: #333;
  padding: 20px;
  background-color: #f9f9f9;
}

.hero-section {
  text-align: center;
  padding: 40px 20px;
  background-color: #00b8d9;
  color: white;
  border-radius: 10px;
  margin-bottom: 20px;
}

.hero-section h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.hero-section p {
  font-size: 1.2rem;
  margin: 0;
}

.content-section {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  margin-top: 20px;
}

.content-section div {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.content-section h2 {
  font-size: 1.8rem;
  color: #00b8d9;
  margin-bottom: 10px;
}

.content-section p,
.content-section ul {
  font-size: 1rem;
  line-height: 1.5;
  margin: 0;
}

.values ul {
  list-style-type: disc;
  padding-left: 20px;
}
</style>
