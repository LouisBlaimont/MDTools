<script>
	import "../app.css";
	let { children } = $props();
</script>



<header class="bg-teal-500 h-16 flex items-center">
	<img alt="Logo MD" src="logo-blanc.png" class="h-full p-2" />
	<nav class="ml-3">	
		<a href="/">home_client</a>
		<a href="/home_2">home_admin</a>
		<a href="/add_group">add_group</a>
		<a href="/login">login</a>
		<a href="/psw_forgot">psw_forgot</a>
		<a href="/sign_up">sign_up</a>
		<a href="/searches">searches_client</a> <!--opens normally when doing a research but for now like this-->
		<a href="/searches_admin">searches_admin</a> <!--opens normally when doing a research but for now like this-->
		<a href="/instrument_edit">instrument_edit</a>
	</nav>
</header>


{@render children()}
