<template>
  <div class="about-us-container">
    <div class="hero-section">
      <h1>About Us</h1>
      <p>Discover our mission, values, and the team driving our innovative dental appointment platform.</p>
    </div>

    <div class="content-section">
      <div class="mission">
        <h2>Our Mission</h2>
        <p>
          Our mission is clear: to build seamless and efficient solutions that connect patients with dentists. We strive to enhance accessibility, making it easy for users to book, manage, and track appointments. By providing an intuitive platform, we aim to improve the dental care experience for both patients and professionals, empowering them to achieve their goals with confidence and convenience.
        </p>
      </div>

      <div class="values">
        <h2>Our Values</h2>
        <ul>
          <li>Integrity: We uphold transparency and honesty in all our processes, ensuring trust with both patients and dentists.</li>
          <li>Innovation: We are committed to continuously evolving our platform to meet the ever-changing needs of our users, integrating new technologies and features to enhance the experience.</li>
          <li>Collaboration: We believe in fostering collaboration between dentists and patients, building a community that helps each other achieve their health goals.</li>
        </ul>
      </div>

      <div class="team">
        <h2>Meet the Team</h2>
        <p>
          Our team is made up of dedicated individuals passionate about improving access to dental care. Each member contributes their expertise to create an innovative platform that bridges the gap between patients and dentists. Together, we work towards a shared vision: making dental care more accessible, efficient, and patient-friendly.
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-us-container {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  padding: 20px;
  background-color: #f7f7f7;
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.hero-section {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #7091E6, #3D52A0);
  color: white;
  border-radius: 10px;
  margin-bottom: 30px;
}

.hero-section h1 {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-section p {
  font-size: 1.2rem;
  margin: 0;
}

.content-section {
  display: grid;
  gap: 30px;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.content-section div {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  text-align: center;
}

.content-section div:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.content-section h2 {
  font-size: 2rem;
  color: #7091E6;
  margin-bottom: 15px;
  border-bottom: 2px solid #7091E6;
  padding-bottom: 5px;
}

.content-section p,
.content-section ul {
  font-size: 1.1rem;
  line-height: 1.6;
  margin: 0;
}

.values ul {
  list-style-type: none;
  padding-left: 0;
}

.values li {
  margin-bottom: 12px;
  line-height: 1.6;
}

.team {
  text-align: center;
}

.team p {
  color: #666;
  font-size: 1.1rem;
  margin-top: 10px;
}
</style>
