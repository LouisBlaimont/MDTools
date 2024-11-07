<script>
	import "../app.css";
	let { children } = $props();
</script>

<nav>	
	<a href="/">home</a>
	<a href="/login">login</a>
	<a href="/psw_forgot">psw_forgot</a>
	<a href="/sign_up">sign_up</a>
	<a href="/home_2">home2.0</a>
	<a href="/searches">searches</a> <!--opens normally when doing a research but for now like this-->
</nav>

<header class="bg-teal-500 h-16 flex items-center">
	<img alt="Logo MD" src="logo-blanc.png" class="h-full p-2" />
 </header>


{@render children()}
