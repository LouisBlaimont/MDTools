<script>
	let { children } = $props();
</script>

<nav>	
	<a href="/">home</a>
	<a href="/login">login</a>
	<a href="/psw_forgot">psw_forgot</a>
	<a href="/sign_up">sign_up</a>
	<a href="/home_2">home2.0</a>
</nav>


{@render children()}
