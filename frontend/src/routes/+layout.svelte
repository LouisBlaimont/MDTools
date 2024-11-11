<script>
	import "../app.css";
	let { children } = $props();
</script>

<nav>	
	<a href="/">home_client</a>
	<a href="/home_2">home_admin</a>
	<a href="/add_group">add_group</a>
	<a href="/login">login</a>
	<a href="/psw_forgot">psw_forgot</a>
	<a href="/sign_up">sign_up</a>
	<a href="/searches">searches_client</a> <!--opens normally when doing a research but for now like this-->
	<a href="/old_searches">old_searches</a> <!--opens normally when doing a research but for now like this-->
</nav>

<header class="bg-teal-500 h-16 flex items-center">
	<img alt="Logo MD" src="logo-blanc.png" class="h-full p-2" />
</header>


{@render children()}
